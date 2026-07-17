import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { SupplierService } from '../../../core/services/supplier.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/models/enums';
import { SupplierSummaryResponse } from '../../../core/models/domain.models';
import { Page } from '../../../core/models/page.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { FilterBarComponent } from '../../../shared/components/filter-bar/filter-bar.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [RouterModule, MatButtonModule, MatIconModule, MatMenuModule, PageHeaderComponent, DataTableComponent, FilterBarComponent],
  template: `
    <app-page-header title="Fornecedores" subtitle="Gerencie fornecedores e seus dados" createLabel="Novo Fornecedor" createRoute="/suppliers/new" requiredRole="MANAGER" />
    <app-filter-bar searchPlaceholder="Buscar por nome ou nome fantasia..." (onSearch)="onSearch($event)" />
    <app-data-table [data]="data()" [columns]="columns" [loading]="loading()" (onPage)="onPageChange($event)" [actionsTemplate]="actionsTemplate" />
    <ng-template #actionsTemplate let-row>
      <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
      <mat-menu #menu="matMenu">
        <button mat-menu-item [routerLink]="['/suppliers', row.id]"><mat-icon>visibility</mat-icon> Ver</button>
        @if (auth.hasRole(UserRole.MANAGER)) { <button mat-menu-item [routerLink]="['/suppliers', row.id, 'edit']"><mat-icon>edit</mat-icon> Editar</button> }
        @if (auth.hasRole(UserRole.ADMIN)) { <button mat-menu-item (click)="onDelete(row)"><mat-icon color="warn">delete</mat-icon> Excluir</button> }
      </mat-menu>
    </ng-template>
  `,
})
export class SupplierListComponent implements OnInit {
  private supplierService = inject(SupplierService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;
  data = signal<Page<SupplierSummaryResponse> | null>(null);
  loading = signal(false);
  private page = 0; private searchTerm = '';

  columns: ColumnDef[] = [
    { key: 'name', header: 'Razão Social', sortable: true },
    { key: 'tradeName', header: 'Nome Fantasia' },
    { key: 'cnpj', header: 'CNPJ' },
  ];

  ngOnInit(): void { this.loadData(); }

  private loadData(): void {
    this.loading.set(true);
    const obs = this.searchTerm
      ? this.supplierService.searchByTradeName(this.searchTerm, this.page)
      : this.supplierService.getAll(this.page);
    obs.pipe(finalize(() => this.loading.set(false)))
      .subscribe({ next: p => this.data.set(p), error: () => this.data.set(null) });
  }

  onSearch(term: string): void { this.searchTerm = term; this.page = 0; this.loadData(); }
  onPageChange(event: PageEvent): void { this.page = event.pageIndex; this.loadData(); }
  onDelete(row: SupplierSummaryResponse): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: 'Excluir Fornecedor', message: 'Excluir "' + row.name + '"?' } })
      .afterClosed().subscribe(c => { if (c) this.supplierService.delete(row.id).subscribe(() => { this.snackBar.open('Fornecedor excluído!', 'OK', { duration: 3000 }); this.loadData(); }); });
  }
}
