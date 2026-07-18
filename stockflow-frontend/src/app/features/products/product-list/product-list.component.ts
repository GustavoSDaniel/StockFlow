import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { finalize, forkJoin } from 'rxjs';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { SupplierService } from '../../../core/services/supplier.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole, ProductStatus, PRODUCT_STATUS_LABELS } from '../../../core/models/enums';
import { ProductResponse, CategoryResponse, SupplierSummaryResponse } from '../../../core/models/domain.models';
import { Page } from '../../../core/models/page.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { FilterBarComponent, FilterField } from '../../../shared/components/filter-bar/filter-bar.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    RouterModule, MatButtonModule, MatIconModule, MatMenuModule,
    PageHeaderComponent, DataTableComponent, FilterBarComponent,
    StatusBadgeComponent, CurrencyBrPipe, EnumLabelPipe, NgTemplateOutlet,
  ],
  template: `
    <app-page-header
      title="Produtos" subtitle="Gerencie o catálogo de produtos"
      createLabel="Novo Produto" createRoute="/products/new" requiredRole="MANAGER"
    />

    <app-filter-bar
      searchPlaceholder="Buscar por nome..."
      [filters]="filterFields"
      (onSearch)="onSearch($event)"
      (onFilterChange)="onFilterChange($event)"
    />

    <app-data-table
      [data]="data()" [columns]="columns" [loading]="loading()"
      (onPage)="onPageChange($event)" (onSort)="onSortChange($event)"
      [actionsTemplate]="actionsTemplate"
    />

    <ng-template #actionsTemplate let-row>
      <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
      <mat-menu #menu="matMenu">
        <button mat-menu-item [routerLink]="['/products', row.id]"><mat-icon>visibility</mat-icon> Ver</button>
        @if (auth.hasRole(UserRole.MANAGER)) {
          <button mat-menu-item [routerLink]="['/products', row.id, 'edit']"><mat-icon>edit</mat-icon> Editar</button>
          @if (row.status === 'INACTIVE' || row.status === 'DISCONTINUED') {
            <button mat-menu-item (click)="onActivate(row)"><mat-icon>toggle_on</mat-icon> Ativar</button>
          }
          @if (row.status === 'ACTIVE') {
            <button mat-menu-item (click)="onInactivate(row)"><mat-icon>toggle_off</mat-icon> Inativar</button>
          }
          @if (row.status === 'ACTIVE' || row.status === 'INACTIVE') {
            <button mat-menu-item (click)="onDiscontinue(row)"><mat-icon>block</mat-icon> Descontinuar</button>
          }
        }
        @if (auth.hasRole(UserRole.ADMIN)) {
          <button mat-menu-item (click)="onDelete(row)"><mat-icon color="warn">delete</mat-icon> Excluir</button>
        }
      </mat-menu>
    </ng-template>
  `,
})
export class ProductListComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private supplierService = inject(SupplierService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;

  data = signal<Page<ProductResponse> | null>(null);
  loading = signal(false);
  private currentPage = 0;
  private currentSize = 10;
  private currentSort = 'name,asc';
  private searchTerm = '';
  private statusFilter = '';

  /** Mapeamento categoryId → categoryName para fallback quando o backend não envia o nome */
  private categoryMap = new Map<string, string>();

  /** Mapeamento supplierId → supplierName para fallback quando o backend não envia o nome */
  private supplierMap = new Map<string, string>();

  filterFields: FilterField[] = [
    { key: 'status', label: 'Status', type: 'select', options: Object.values(ProductStatus).map(s => ({ value: s, label: PRODUCT_STATUS_LABELS[s] })) },
  ];

  columns: ColumnDef[] = [
    { key: 'name', header: 'Nome', sortable: true },
    { key: 'sku', header: 'SKU' },
    { key: 'categoryName', header: 'Categoria', cell: (r: ProductResponse) => r.categoryName || this.categoryMap.get(r.categoryId) || r.categoryId },
    { key: 'supplierName', header: 'Fornecedor', cell: (r: ProductResponse) => r.supplierName || this.supplierMap.get(r.supplierId) || r.supplierId },
    { key: 'costPrice', header: 'Preço Custo', cell: (r) => new CurrencyBrPipe().transform(r.costPrice) },
    { key: 'salePrice', header: 'Preço Venda', cell: (r) => new CurrencyBrPipe().transform(r.salePrice) },
    { key: 'unitMeasure', header: 'Unidade', cell: (r) => new EnumLabelPipe().transform(r.unitMeasure, 'unitMeasure') },
    { key: 'status', header: 'Status' },
  ];

  ngOnInit(): void {
    // Carrega categorias e fornecedores em paralelo para fallback de nomes
    forkJoin([
      this.categoryService.getAll(0, 100),
      this.supplierService.getAll(0, 500),
    ]).subscribe(([catPage, supPage]) => {
      this.buildCategoryMap(catPage.content);
      for (const s of supPage.content) {
        this.supplierMap.set(s.id, s.name);
      }
      this.loadData();
    });
  }

  /** Constrói um mapa plano categoryId → nome a partir da árvore de categorias */
  private buildCategoryMap(categories: CategoryResponse[]): void {
    const walk = (list: CategoryResponse[]): void => {
      for (const c of list) {
        this.categoryMap.set(c.id, c.name);
        if (c.subCategories?.length) walk(c.subCategories);
      }
    };
    walk(categories);
  }

  private loadData(): void {
    this.loading.set(true);
    let obs;
    if (this.searchTerm && this.statusFilter) {
      obs = this.productService.searchByNameAndStatus(this.searchTerm, this.statusFilter as ProductStatus, this.currentPage, this.currentSize);
    } else if (this.searchTerm) {
      obs = this.productService.searchByName(this.searchTerm, this.currentPage, this.currentSize);
    } else if (this.statusFilter) {
      obs = this.productService.getByStatus(this.statusFilter as ProductStatus, this.currentPage, this.currentSize);
    } else {
      obs = this.productService.getAll(this.currentPage, this.currentSize, this.currentSort);
    }
    obs.pipe(finalize(() => this.loading.set(false)))
      .subscribe({ next: page => this.data.set(page), error: () => this.data.set(null) });
  }

  onSearch(term: string): void { this.searchTerm = term; this.currentPage = 0; this.loadData(); }
  onFilterChange(filters: Record<string, string>): void { this.statusFilter = filters['status'] || ''; this.currentPage = 0; this.loadData(); }
  onPageChange(event: PageEvent): void { this.currentPage = event.pageIndex; this.currentSize = event.pageSize; this.loadData(); }
  onSortChange(sort: Sort): void { this.currentSort = sort.active + ',' + (sort.direction || 'asc'); this.loadData(); }

  onDelete(row: ProductResponse): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Excluir Produto', message: `Tem certeza que deseja excluir "${row.name}"?`, confirmLabel: 'Excluir' }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.productService.delete(row.id).subscribe(() => {
          this.snackBar.open('Produto excluído!', 'OK', { duration: 3000 });
          this.loadData();
        });
      }
    });
  }

  onActivate(row: ProductResponse): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Ativar Produto', message: `Deseja ativar "${row.name}"?`, confirmLabel: 'Ativar' }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.productService.activate(row.id).subscribe(() => {
          this.snackBar.open('Produto ativado!', 'OK', { duration: 3000 });
          this.loadData();
        });
      }
    });
  }

  onInactivate(row: ProductResponse): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Inativar Produto', message: `Deseja inativar "${row.name}"? O produto não aparecerá em novas operações.`, confirmLabel: 'Inativar' }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.productService.inactive(row.id).subscribe(() => {
          this.snackBar.open('Produto inativado!', 'OK', { duration: 3000 });
          this.loadData();
        });
      }
    });
  }

  onDiscontinue(row: ProductResponse): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Descontinuar Produto', message: `Deseja descontinuar "${row.name}"? Esta ação indica que o produto não será mais comercializado.`, confirmLabel: 'Descontinuar' }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.productService.discontinue(row.id).subscribe(() => {
          this.snackBar.open('Produto descontinuado!', 'OK', { duration: 3000 });
          this.loadData();
        });
      }
    });
  }
}
