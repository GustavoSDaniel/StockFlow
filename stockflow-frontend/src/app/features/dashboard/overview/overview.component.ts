import { Component, inject, OnInit, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DashboardOverviewResponse } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';

@Component({
  selector: 'app-overview-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, LoadingSpinnerComponent, CurrencyBrPipe],
  template: `
    <h2>Visão Geral</h2>
    @if (loading()) { <app-loading-spinner /> }
    @else if (data()) {
      <div class="kpi-grid">
        <mat-card class="kpi"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#7c3aed">category</mat-icon>
          <div class="kpi-value">{{ data()?.totalProducts }}</div><div class="kpi-label">Total Produtos</div>
          <div class="kpi-detail">Ativos: {{ data()?.activeProducts }} | Inativos: {{ data()?.inactiveProducts }} | Descontinuados: {{ data()?.discontinuedProducts }}</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#16a34a">attach_money</mat-icon>
          <div class="kpi-value">{{ data()?.totalStockValue | currencyBr }}</div><div class="kpi-label">Valor Total em Estoque</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#2563eb">trending_up</mat-icon>
          <div class="kpi-value">{{ data()?.potentialSalesValue | currencyBr }}</div><div class="kpi-label">Potencial de Vendas</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#d97706">percent</mat-icon>
          <div class="kpi-value">{{ data()?.averageMargin?.toFixed(1) }}%</div><div class="kpi-label">Margem Média</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#0891b2">business</mat-icon>
          <div class="kpi-value">{{ data()?.totalSuppliers }}</div><div class="kpi-label">Fornecedores</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#7c3aed">folder</mat-icon>
          <div class="kpi-value">{{ data()?.totalCategories }}</div><div class="kpi-label">Categorias</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi warning"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#dc2626">warning</mat-icon>
          <div class="kpi-value">{{ data()?.pendingCriticalNotifications }}</div><div class="kpi-label">Notificações Críticas</div>
        </mat-card-content></mat-card>

        <mat-card class="kpi warning"><mat-card-content>
          <mat-icon class="kpi-icon" style="color:#d97706">error</mat-icon>
          <div class="kpi-value">{{ data()?.pendingHighNotifications }}</div><div class="kpi-label">Notificações Alta Prioridade</div>
        </mat-card-content></mat-card>
      </div>
    }
  `,
  styles: [`
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; }
    .kpi { border-radius: 8px; }
    .kpi-icon { font-size: 32px; width: 32px; height: 32px; margin-bottom: 8px; }
    .kpi-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }
    .kpi-label { font-size: 14px; color: #666; margin-top: 4px; }
    .kpi-detail { font-size: 12px; color: #999; margin-top: 8px; }
    .warning { background: #fef2f2; }
  `],
})
export class OverviewDashboardComponent implements OnInit {
  private ds = inject(DashboardService);
  data = signal<DashboardOverviewResponse | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.ds.getOverview().subscribe(d => { this.data.set(d); this.loading.set(false); });
  }
}
