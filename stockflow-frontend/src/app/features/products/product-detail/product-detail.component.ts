import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../../../core/services/product.service';
import { StockService } from '../../../core/services/stock.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ProductResponse, StockResponse } from '../../../core/models/domain.models';
import { UserRole } from '../../../core/models/enums';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { StockStatusIndicatorComponent } from '../../../shared/components/stock-status-indicator/stock-status-indicator.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    RouterModule, MatCardModule, MatTabsModule, MatButtonModule, MatIconModule,
    StatusBadgeComponent, StockStatusIndicatorComponent, LoadingSpinnerComponent,
    CurrencyBrPipe, EnumLabelPipe,
  ],
  template: `
    <div class="detail-header">
      <button mat-icon-button routerLink="/products"><mat-icon>arrow_back</mat-icon></button>
      <h2>{{ product()?.name || 'Detalhes do Produto' }}</h2>
      <span class="spacer"></span>
      @if (auth.hasRole(UserRole.MANAGER)) {
        <button mat-flat-button color="primary" [routerLink]="['/products', product()?.id, 'edit']">
          <mat-icon>edit</mat-icon> Editar
        </button>
      }
    </div>

    @if (loading()) { <app-loading-spinner /> }
    @else if (product()) {
      <mat-card class="info-card">
        <mat-card-content>
          <div class="info-grid">
            <div><strong>SKU:</strong> {{ product()?.sku }}</div>
            <div><strong>Código de Barras:</strong> {{ product()?.barcode || '-' }}</div>
            <div><strong>Categoria:</strong> {{ product()?.categoryName }}</div>
            <div><strong>Fornecedor:</strong> {{ product()?.supplierName }}</div>
            <div><strong>Preço Custo:</strong> {{ product()?.costPrice | currencyBr }}</div>
            <div><strong>Preço Venda:</strong> {{ product()?.salePrice | currencyBr }}</div>
            <div><strong>Margem:</strong> {{ product()?.margin }}%</div>
            <div><strong>Unidade:</strong> {{ product()?.unitMeasure | enumLabel:'unitMeasure' }}</div>
            <div><strong>Status:</strong> <app-status-badge [label]="product()?.status || ''" /></div>
            @if (product()?.description) {
              <div class="full-width"><strong>Descrição:</strong> {{ product()?.description }}</div>
            }
          </div>
        </mat-card-content>
      </mat-card>

      <mat-tab-group class="tabs">
        <mat-tab label="Estoque ({{ stocks().length }})">
          @for (s of stocks(); track s.id) {
            <mat-card class="stock-card">
              <mat-card-content>
                <div class="stock-row">
                  <app-stock-status-indicator [status]="s.stockStatus" [quantity]="s.currentQuantity" [showQuantity]="true" />
                  <div><strong>Warehouse:</strong> {{ s.warehouseId }}</div>
                  <div><strong>Local:</strong> {{ s.location }}</div>
                  <div><strong>Mín:</strong> {{ s.minimumQuantity }} | <strong>Máx:</strong> {{ s.maximumQuantity }}</div>
                  <div><strong>Reposição:</strong> {{ s.reorderPoint }} → {{ s.reorderQuantity }}</div>
                  <button mat-stroked-button [routerLink]="['/stocks', s.id]">Ver Detalhes</button>
                  <button mat-stroked-button color="primary" [routerLink]="['/stocks', s.id, 'movement']">Movimentar</button>
                </div>
              </mat-card-content>
            </mat-card>
          } @empty { <p class="no-data">Nenhum estoque configurado para este produto.</p> }
        </mat-tab>
      </mat-tab-group>
    }
  `,
  styles: [`
    .detail-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .detail-header h2 { margin: 0; } .spacer { flex: 1; }
    .info-card { margin-bottom: 24px; }
    .info-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 12px; }
    .full-width { grid-column: 1 / -1; }
    .stock-card { margin: 12px 0; }
    .stock-row { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
    .no-data { color: #999; padding: 24px; text-align: center; }
    .tabs { margin-top: 16px; }
  `],
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private stockService = inject(StockService);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;

  product = signal<ProductResponse | null>(null);
  stocks = signal<StockResponse[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.productService.getById(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(p => this.product.set(p));

    this.stockService.getByProduct(id).subscribe(s => this.stocks.set(s.content));
  }
}
