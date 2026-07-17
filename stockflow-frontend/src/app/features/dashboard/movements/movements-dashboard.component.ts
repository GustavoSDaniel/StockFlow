import { Component, inject, OnInit, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DashboardMovementsResponse } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-movements-dashboard',
  standalone: true,
  imports: [MatCardModule, MatListModule, LoadingSpinnerComponent, EnumLabelPipe],
  template: `
    <h2>Dashboard de Movimentações</h2>
    @if (loading()) { <app-loading-spinner /> }
    @else if (data()) {
      <div class="kpi-grid">
        <mat-card><mat-card-content><div class="label">Hoje</div><div class="val">{{ data()?.todayMovements }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="label">Entradas (Mês)</div><div class="val green">{{ data()?.monthEntries }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="label">Saídas (Mês)</div><div class="val red">{{ data()?.monthExits }}</div></mat-card-content></mat-card>
      </div>

      <div class="lists">
        <mat-card>
          <mat-card-header><mat-card-title>Por Tipo</mat-card-title></mat-card-header>
          <mat-card-content><mat-list>
            @for (m of data()?.movementsByType; track m.movementType) {
              <mat-list-item><span matListItemTitle>{{ m.movementType | enumLabel:'movementType' }}</span><span matListItemLine>{{ m.count }} mov. - Qtd total: {{ m.totalQuantity }}</span></mat-list-item>
            }
          </mat-list></mat-card-content>
        </mat-card>

        <mat-card>
          <mat-card-header><mat-card-title>Top 10 Produtos Movimentados</mat-card-title></mat-card-header>
          <mat-card-content><mat-list>
            @for (p of data()?.top10MovedProducts; track p.productId) {
              <mat-list-item><span matListItemTitle>{{ p.productName }}</span><span matListItemLine>Qtd movimentada: {{ p.totalMoved }}</span></mat-list-item>
            }
          </mat-list></mat-card-content>
        </mat-card>
      </div>
    }
  `,
  styles: [`.kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 24px; } .val { font-size: 32px; font-weight: 700; } .green { color: #16a34a; } .red { color: #dc2626; } .label { color: #666; font-size: 14px; } .lists { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; } @media(max-width:768px){ .kpi-grid,.lists { grid-template-columns: 1fr; } }`],
})
export class MovementsDashboardComponent implements OnInit {
  private ds = inject(DashboardService);
  data = signal<DashboardMovementsResponse | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.ds.getMovements().subscribe(d => { this.data.set(d); this.loading.set(false); });
  }
}
