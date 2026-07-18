import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DashboardStockResponse } from '../../../core/models/domain.models';
import { StockStatus } from '../../../core/models/enums';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StockStatusIndicatorComponent } from '../../../shared/components/stock-status-indicator/stock-status-indicator.component';

@Component({
  selector: 'app-stock-dashboard',
  standalone: true,
  imports: [RouterModule, MatCardModule, MatIconModule, MatListModule, LoadingSpinnerComponent, StockStatusIndicatorComponent],
  template: `
    <h2>Dashboard de Estoque</h2>
    @if (loading()) { <app-loading-spinner /> }
    @else if (data()) {
      <div class="kpi-grid">
        <mat-card><mat-card-content><app-stock-status-indicator [status]="status.OUT_OF_STOCK" /><div class="val">{{ data()?.outOfStockCount }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><app-stock-status-indicator [status]="status.LOW" /><div class="val">{{ data()?.lowStockCount }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><app-stock-status-indicator [status]="status.REORDER_POINT" /><div class="val">{{ data()?.reorderPointCount }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><app-stock-status-indicator [status]="status.NORMAL" /><div class="val">{{ data()?.normalCount }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><app-stock-status-indicator [status]="status.OVER_STOCKED" /><div class="val">{{ data()?.overStockedCount }}</div></mat-card-content></mat-card>
      </div>

      <div class="lists">
        <mat-card>
          <mat-card-header><mat-card-title>Top 10 - Estoque Mais Baixo</mat-card-title></mat-card-header>
          <mat-card-content>
            <mat-list>
              @for (item of data()?.top10LowStock; track item.productId) {
                <mat-list-item><app-stock-status-indicator matListItemIcon [status]="item.status" /><span matListItemTitle>{{ item.productName }}</span><span matListItemLine>{{ item.sku }} - Qtd: {{ item.currentQuantity }}</span></mat-list-item>
              }
            </mat-list>
          </mat-card-content>
        </mat-card>

        <mat-card>
          <mat-card-header><mat-card-title>Top 10 - Estoque Mais Alto</mat-card-title></mat-card-header>
          <mat-card-content>
            <mat-list>
              @for (item of data()?.top10HighStock; track item.productId) {
                <mat-list-item><app-stock-status-indicator matListItemIcon [status]="item.status" /><span matListItemTitle>{{ item.productName }}</span><span matListItemLine>{{ item.sku }} - Qtd: {{ item.currentQuantity }}</span></mat-list-item>
              }
            </mat-list>
          </mat-card-content>
        </mat-card>
      </div>
    }
  `,
  styles: [`.kpi-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; margin-bottom: 24px; } .val { font-size: 32px; font-weight: 700; margin-top: 8px; } .lists { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; } @media(max-width:768px){ .lists { grid-template-columns: 1fr; } }`],
})
export class StockDashboardComponent implements OnInit {
  private ds = inject(DashboardService);
  data = signal<DashboardStockResponse | null>(null);
  loading = signal(true);
  status = StockStatus;

  ngOnInit(): void {
    this.ds.getStocks().subscribe(d => { this.data.set(d); this.loading.set(false); });
  }
}
