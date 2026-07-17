import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTabsModule } from '@angular/material/tabs';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { StockService } from '../../../core/services/stock.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/models/enums';
import { StockSummaryResponse } from '../../../core/models/domain.models';
import { Page } from '../../../core/models/page.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { StockStatusIndicatorComponent } from '../../../shared/components/stock-status-indicator/stock-status-indicator.component';
import { finalize } from 'rxjs';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-stock-list',
  standalone: true,
  imports: [
    RouterModule, MatButtonModule, MatIconModule, MatMenuModule, MatTabsModule,
    PageHeaderComponent, DataTableComponent, StockStatusIndicatorComponent, NgTemplateOutlet,
  ],
  template: `
    <app-page-header title="Estoque" subtitle="Gerencie o inventário por warehouse" />

    <mat-tab-group (selectedIndexChange)="onTabChange($event)" class="tabs">
      <mat-tab label="Todos">
        <app-data-table
          [data]="data()" [columns]="columns" [loading]="loading()"
          (onPage)="onPageChange($event)" (onSort)="onSortChange($event)"
          [actionsTemplate]="actionsTemplate"
        />
      </mat-tab>
      <mat-tab label="Estoque Baixo">
        <app-data-table
          [data]="data()" [columns]="columns" [loading]="loading()"
          (onPage)="onPageChange($event)" (onSort)="onSortChange($event)"
          [actionsTemplate]="actionsTemplate"
        />
      </mat-tab>
      <mat-tab label="Sem Estoque">
        <app-data-table
          [data]="data()" [columns]="columns" [loading]="loading()"
          (onPage)="onPageChange($event)" (onSort)="onSortChange($event)"
          [actionsTemplate]="actionsTemplate"
        />
      </mat-tab>
      <mat-tab label="Excesso">
        <app-data-table
          [data]="data()" [columns]="columns" [loading]="loading()"
          (onPage)="onPageChange($event)" (onSort)="onSortChange($event)"
          [actionsTemplate]="actionsTemplate"
        />
      </mat-tab>
    </mat-tab-group>

    <ng-template #actionsTemplate let-row>
      <button mat-icon-button [matMenuTriggerFor]="menu">
        <mat-icon>more_vert</mat-icon>
      </button>
      <mat-menu #menu="matMenu">
        <button mat-menu-item [routerLink]="['/stocks', row.id]"><mat-icon>visibility</mat-icon> Ver</button>
        <button mat-menu-item [routerLink]="['/stocks', row.id, 'movement']"><mat-icon>swap_vert</mat-icon> Movimentar</button>
      </mat-menu>
    </ng-template>
  `,
  styles: [`.tabs { background: #fff; border-radius: 8px; }`]
})
export class StockListComponent implements OnInit {
  private stockService = inject(StockService);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;

  data = signal<Page<StockSummaryResponse> | null>(null);
  loading = signal(false);
  private currentTab = 0;
  private currentPage = 0;
  private currentSize = 10;

  columns: ColumnDef[] = [
    { key: 'productName', header: 'Produto', sortable: true },
    { key: 'productSku', header: 'SKU' },
    { key: 'warehouseId', header: 'Warehouse' },
    { key: 'currentQuantity', header: 'Qtd Atual' },
    { key: 'minimumQuantity', header: 'Qtd Mínima' },
    { key: 'stockStatus', header: 'Status' },
  ];

  ngOnInit(): void { this.loadData(); }

  onTabChange(index: number): void { this.currentTab = index; this.currentPage = 0; this.loadData(); }

  private loadData(): void {
    this.loading.set(true);
    let obs;
    switch (this.currentTab) {
      case 1: obs = this.stockService.getLowStock(this.currentPage, this.currentSize); break;
      case 2: obs = this.stockService.getOutOfStock(this.currentPage, this.currentSize); break;
      case 3: obs = this.stockService.getOverStock(this.currentPage, this.currentSize); break;
      default: obs = this.stockService.getAll(this.currentPage, this.currentSize);
    }
    obs.pipe(finalize(() => this.loading.set(false)))
      .subscribe({ next: p => this.data.set(p), error: () => this.data.set(null) });
  }

  onPageChange(event: PageEvent): void { this.currentPage = event.pageIndex; this.currentSize = event.pageSize; this.loadData(); }
  onSortChange(sort: Sort): void { this.loadData(); }
}
