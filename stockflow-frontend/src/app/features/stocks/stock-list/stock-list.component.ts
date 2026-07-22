import { Component, inject, OnInit, signal, TemplateRef, ViewChild } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTabsModule } from '@angular/material/tabs';
import { PageEvent } from '@angular/material/paginator';
import { StockService } from '../../../core/services/stock.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/models/enums';
import { StockSummaryResponse } from '../../../core/models/domain.models';
import { Page } from '../../../core/models/page.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { catchError, EMPTY, finalize } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-stock-list',
  standalone: true,
  imports: [
    RouterModule, MatButtonModule, MatIconModule, MatMenuModule, MatTabsModule,
    PageHeaderComponent, DataTableComponent, StatusBadgeComponent,
  ],
  template: `
    <app-page-header title="Estoque" subtitle="Gerencie o inventário por galpão">
      <button mat-stroked-button [matMenuTriggerFor]="pdfMenu" [disabled]="isExportingPdf()">
        <mat-icon>{{ isExportingPdf() ? 'hourglass_empty' : 'picture_as_pdf' }}</mat-icon>
        {{ isExportingPdf() ? 'Exportando...' : 'Exportar PDF' }}
      </button>
      <mat-menu #pdfMenu="matMenu">
        <button mat-menu-item (click)="exportReportToPdf()">
          <mat-icon>picture_as_pdf</mat-icon> Baixar Relatório Completo
        </button>
        <button mat-menu-item (click)="exportReportToPdf('OUT_OF_STOCK')">
          <mat-icon>inventory_2</mat-icon> Baixar Itens Sem Estoque
        </button>
        <button mat-menu-item (click)="exportReportToPdf('LOW')">
          <mat-icon>warning</mat-icon> Baixar Estoque Baixo
        </button>
        <button mat-menu-item (click)="exportReportToPdf('OVER_STOCKED')">
          <mat-icon>warehouse</mat-icon> Baixar Excesso de Estoque
        </button>
      </mat-menu>
    </app-page-header>

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

    <!-- Template de badge para a coluna Status -->
    <ng-template #statusCell let-row>
      <app-status-badge [label]="row.status" />
    </ng-template>

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
  private snackBar = inject(MatSnackBar);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;

  @ViewChild('statusCell', { static: true }) statusCellTemplate!: TemplateRef<any>;

  data = signal<Page<StockSummaryResponse> | null>(null);
  loading = signal(false);
  isExportingPdf = signal(false);
  private currentTab = 0;
  private currentPage = 0;
  private currentSize = 10;

  columns: ColumnDef[] = [];

  ngOnInit(): void {
    // Define as colunas aqui para usar o template capturado via ViewChild
    this.columns = [
      { key: 'productName', header: 'Produto', sortable: true },
      { key: 'sku', header: 'SKU' },
      { key: 'warehouseId', header: 'Galpão', cell: (r: StockSummaryResponse) => r.warehouseId || 'Não informado' },
      { key: 'location', header: 'Prateleira', cell: (r: StockSummaryResponse) => r.location || 'Não informado' },
      { key: 'currentQuantity', header: 'Qtd Atual' },
      { key: 'status', header: 'Status', cellTemplate: this.statusCellTemplate },
    ];
    this.loadData();
  }

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
  onSortChange(sort: any): void { this.loadData(); }

  exportReportToPdf(status?: string): void {
    this.isExportingPdf.set(true);
    this.stockService.downloadPdfReport(status)
      .pipe(
        catchError(() => {
          this.snackBar.open('Erro ao exportar relatório PDF. Tente novamente.', 'OK', { duration: 5000 });
          return EMPTY;
        }),
        finalize(() => this.isExportingPdf.set(false)),
      )
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = status
          ? `relatorio_estoque_${status.toLowerCase()}.pdf`
          : 'relatorio_estoque.pdf';
        anchor.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Relatório exportado com sucesso!', 'OK', { duration: 3000 });
      });
  }
}
