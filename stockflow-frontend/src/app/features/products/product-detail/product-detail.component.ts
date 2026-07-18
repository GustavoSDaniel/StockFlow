import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { StockService } from '../../../core/services/stock.service';
import { CategoryService } from '../../../core/services/category.service';
import { SupplierService } from '../../../core/services/supplier.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ProductResponse, StockResponse, StockRequest, CategoryResponse } from '../../../core/models/domain.models';
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
    RouterModule, ReactiveFormsModule, MatCardModule, MatTabsModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatInputModule,
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
            <div><strong>Categoria:</strong> {{ categoryDisplayName() }}</div>
            <div><strong>Fornecedor:</strong> {{ supplierDisplayName() }}</div>
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
          @if (auth.hasRole(UserRole.MANAGER)) {
            <div class="add-stock-bar">
              @if (!showStockForm()) {
                <button mat-stroked-button color="primary" (click)="showStockForm.set(true)">
                  <mat-icon>add</mat-icon> Adicionar Estoque
                </button>
              } @else {
                <mat-card class="stock-form-card">
                  <mat-card-content>
                    <h3>Novo Estoque</h3>
                    <form [formGroup]="stockForm" (ngSubmit)="onSubmitStock()">
                      <div class="form-grid">
                        <mat-form-field appearance="outline">
                          <mat-label>Galpão</mat-label>
                          <input matInput formControlName="warehouseId" required placeholder="Ex: WH-001" />
                        </mat-form-field>
                        <mat-form-field appearance="outline">
                          <mat-label>Localização</mat-label>
                          <input matInput formControlName="location" required placeholder="Ex: Prateleira A3" />
                        </mat-form-field>
                        <mat-form-field appearance="outline">
                          <mat-label>Quantidade Mínima</mat-label>
                          <input matInput type="number" formControlName="minimumQuantity" required min="0" />
                        </mat-form-field>
                        <mat-form-field appearance="outline">
                          <mat-label>Quantidade Máxima</mat-label>
                          <input matInput type="number" formControlName="maximumQuantity" required min="1" />
                        </mat-form-field>
                        <mat-form-field appearance="outline">
                          <mat-label>Ponto de Reposição</mat-label>
                          <input matInput type="number" formControlName="reorderPoint" required min="0" />
                        </mat-form-field>
                        <mat-form-field appearance="outline">
                          <mat-label>Qtd. Reposição</mat-label>
                          <input matInput type="number" formControlName="reorderQuantity" required min="1" />
                        </mat-form-field>
                      </div>
                      <div class="form-actions">
                        <button mat-button type="button" (click)="showStockForm.set(false)">Cancelar</button>
                        <button mat-flat-button color="primary" type="submit" [disabled]="stockForm.invalid || stockSaving()">
                          {{ stockSaving() ? 'Salvando...' : 'Salvar' }}
                        </button>
                      </div>
                    </form>
                  </mat-card-content>
                </mat-card>
              }
            </div>
          }
          @for (s of stocks(); track s.id) {
            <mat-card class="stock-card">
              <mat-card-content>
                <div class="stock-row">
                  <app-stock-status-indicator [status]="s.stockStatus" [quantity]="s.currentQuantity" [showQuantity]="true" />
                  <div><strong>Galpão:</strong> {{ s.warehouseId || 'Não informado' }}</div>
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
    .add-stock-bar { margin: 16px 0; }
    .stock-form-card { margin-bottom: 16px; border: 1px solid #7c3aed; border-radius: 12px; }
    .stock-form-card h3 { margin: 0 0 16px; color: #1a1a2e; font-size: 16px; }
    .form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
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
  private categoryService = inject(CategoryService);
  private supplierService = inject(SupplierService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;

  product = signal<ProductResponse | null>(null);
  stocks = signal<StockResponse[]>([]);
  loading = signal(true);

  /** Mapeamento categoryId → categoryName para fallback quando o backend não envia o nome */
  private categoryMap = new Map<string, string>();

  /** Mapeamento supplierId → supplierName para fallback quando o backend não envia o nome */
  private supplierMap = new Map<string, string>();

  /** Controla a exibição do formulário de criação de estoque */
  showStockForm = signal(false);
  stockSaving = signal(false);

  stockForm: FormGroup = this.fb.group({
    warehouseId: ['', Validators.required],
    location: ['', Validators.required],
    minimumQuantity: [0, [Validators.required, Validators.min(0)]],
    maximumQuantity: [100, [Validators.required, Validators.min(1)]],
    reorderPoint: [10, [Validators.required, Validators.min(0)]],
    reorderQuantity: [50, [Validators.required, Validators.min(1)]],
  });

  /** Nome da categoria para exibição: tenta categoryName do backend, fallback no mapa local */
  categoryDisplayName(): string {
    const p = this.product();
    if (!p) return '-';
    return p.categoryName || this.categoryMap.get(p.categoryId) || p.categoryId;
  }

  /** Nome do fornecedor para exibição: tenta supplierName do backend, fallback no mapa local */
  supplierDisplayName(): string {
    const p = this.product();
    if (!p) return '-';
    return p.supplierName || this.supplierMap.get(p.supplierId) || p.supplierId;
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;

    // Carrega categorias e fornecedores para fallback de nome
    this.categoryService.getAll(0, 100).subscribe(catPage => {
      this.buildCategoryMap(catPage.content);
    });
    this.supplierService.getAll(0, 500).subscribe(supPage => {
      for (const s of supPage.content) {
        this.supplierMap.set(s.id, s.name);
      }
    });

    this.productService.getById(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(p => this.product.set(p));

    this.loadStocks(id);
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

  private loadStocks(productId: string): void {
    this.stockService.getByProduct(productId).subscribe(s => this.stocks.set(s.content));
  }

  /** Submete o formulário de criação de estoque */
  onSubmitStock(): void {
    if (this.stockForm.invalid) return;
    const productId = this.product()?.id;
    if (!productId) return;

    this.stockSaving.set(true);
    const req: StockRequest = this.stockForm.value;

    this.stockService.create(productId, req).subscribe({
      next: () => {
        this.snackBar.open('Estoque criado com sucesso!', 'OK', { duration: 3000 });
        this.stockSaving.set(false);
        this.showStockForm.set(false);
        this.stockForm.reset({
          warehouseId: '', location: '',
          minimumQuantity: 0, maximumQuantity: 100,
          reorderPoint: 10, reorderQuantity: 50,
        });
        this.loadStocks(productId);
      },
      error: () => {
        this.snackBar.open('Erro ao criar estoque.', 'OK', { duration: 3000 });
        this.stockSaving.set(false);
      },
    });
  }
}
