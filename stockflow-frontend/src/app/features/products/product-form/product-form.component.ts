import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { ProductService } from '../../../core/services/product.service';
import { CategoryService } from '../../../core/services/category.service';
import { SupplierService } from '../../../core/services/supplier.service';
import { CategoryResponse, SupplierSummaryResponse } from '../../../core/models/domain.models';
import { UnitMeasure, UNIT_MEASURE_LABELS } from '../../../core/models/enums';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatCardModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="form-header">
      <button mat-icon-button routerLink="/products"><mat-icon>arrow_back</mat-icon></button>
      <h2>{{ isEdit ? 'Editar Produto' : 'Novo Produto' }}</h2>
    </div>

    @if (loading()) { <app-loading-spinner message="Carregando..." /> }
    @else {
      <mat-card>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-grid">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Nome</mat-label><input matInput formControlName="name" required />
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Descrição</mat-label><textarea matInput formControlName="description" rows="2"></textarea>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Categoria</mat-label>
              <mat-select formControlName="categoryId" required>
                @for (cat of categories(); track cat.id) { <mat-option [value]="cat.id">{{ cat.name }}</mat-option> }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Fornecedor</mat-label>
              <mat-select formControlName="supplierId" required>
                @for (sup of suppliers(); track sup.id) { <mat-option [value]="sup.id">{{ sup.tradeName || sup.name }}</mat-option> }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Preço de Custo</mat-label>
              <input matInput type="number" formControlName="costPrice" required step="0.01" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Preço de Venda</mat-label>
              <input matInput type="number" formControlName="salePrice" required step="0.01" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Unidade de Medida</mat-label>
              <mat-select formControlName="unitMeasure" required>
                @for (um of unitMeasures; track um.value) { <mat-option [value]="um.value">{{ um.label }}</mat-option> }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Código de Barras</mat-label><input matInput formControlName="barcode" />
            </mat-form-field>

            <div class="form-actions">
              <button mat-button type="button" routerLink="/products">Cancelar</button>
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">
                {{ isEdit ? 'Salvar' : 'Criar' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`
    .form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .form-header h2 { margin: 0; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .full-width { grid-column: 1 / -1; }
    .form-actions { grid-column: 1 / -1; display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    @media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } }
  `],
})
export class ProductFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private supplierService = inject(SupplierService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  isEdit = false;
  private productId = '';
  loading = signal(true);
  saving = signal(false);
  categories = signal<CategoryResponse[]>([]);
  suppliers = signal<SupplierSummaryResponse[]>([]);
  unitMeasures = Object.values(UnitMeasure).map(v => ({ value: v, label: UNIT_MEASURE_LABELS[v] }));

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    categoryId: ['', Validators.required],
    supplierId: ['', Validators.required],
    costPrice: [0, [Validators.required, Validators.min(0)]],
    salePrice: [0, [Validators.required, Validators.min(0)]],
    unitMeasure: [UnitMeasure.UN, Validators.required],
    barcode: [''],
  });

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('id') || '';
    this.isEdit = !!this.productId;

    // Load dropdowns
    this.categoryService.getAll(0, 100).subscribe(c => this.categories.set(c.content));
    this.supplierService.getAll(0, 100).subscribe(s => this.suppliers.set(s.content));

    if (this.isEdit) {
      this.productService.getById(this.productId)
        .pipe(finalize(() => this.loading.set(false)))
        .subscribe(p => {
          this.form.patchValue({
            name: p.name, description: p.description,
            categoryId: p.categoryId, supplierId: p.supplierId,
            costPrice: p.costPrice, salePrice: p.salePrice,
            unitMeasure: p.unitMeasure, barcode: p.barcode,
          });
        });
    } else {
      this.loading.set(false);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const data = this.form.value;

    const req = this.isEdit
      ? this.productService.update(this.productId, {
          name: data.name, description: data.description,
          costPrice: data.costPrice, salePrice: data.salePrice,
          unitMeasure: data.unitMeasure, barcode: data.barcode,
        })
      : this.productService.create(data);

    req.subscribe({
      next: () => {
        this.snackBar.open(this.isEdit ? 'Produto atualizado!' : 'Produto criado!', 'OK', { duration: 3000 });
        this.router.navigate(['/products']);
      },
      error: () => this.saving.set(false),
    });
  }
}
