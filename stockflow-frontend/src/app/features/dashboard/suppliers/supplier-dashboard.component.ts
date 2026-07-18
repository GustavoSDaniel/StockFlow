import { Component, inject, OnInit, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DashboardSupplierResponse } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';

@Component({
  selector: 'app-supplier-dashboard',
  standalone: true,
  imports: [MatCardModule, MatListModule, LoadingSpinnerComponent, CurrencyBrPipe],
  template: `
    <h2>Dashboard de Fornecedores</h2>
    @if (loading()) { <app-loading-spinner /> }
    @else {
      <div class="supplier-grid">
        @for (s of data()?.suppliers; track s.supplierId) {
          <mat-card>
            <mat-card-header>
              <mat-card-title>{{ s.tradeName || s.supplierName }}</mat-card-title>
              @if (s.tradeName && s.supplierName && s.tradeName !== s.supplierName) {
                <mat-card-subtitle>{{ s.supplierName }}</mat-card-subtitle>
              }
            </mat-card-header>
            <mat-card-content>
              <div class="stat"><span class="num">{{ s.productCount }}</span> produtos</div>
              <div class="stat">Valor estoque: <strong>{{ s.totalStockValue | currencyBr }}</strong></div>
            </mat-card-content>
          </mat-card>
        }
      </div>
    }
  `,
  styles: [`.supplier-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; } .stat { margin: 4px 0; } .num { font-size: 24px; font-weight: 700; color: #7c3aed; }`],
})
export class SupplierDashboardComponent implements OnInit {
  private ds = inject(DashboardService);
  data = signal<DashboardSupplierResponse | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.ds.getSuppliers().subscribe(d => { this.data.set(d); this.loading.set(false); });
  }
}
