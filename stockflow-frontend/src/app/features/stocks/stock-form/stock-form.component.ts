import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { StockService } from '../../../core/services/stock.service';
import { StockResponse } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-stock-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatCardModule, MatIconModule, LoadingSpinnerComponent],
  template: `
    <div class="form-header">
      <button mat-icon-button routerLink="/stocks"><mat-icon>arrow_back</mat-icon></button>
      <h2>{{ isEdit ? 'Editar Configuração de Estoque' : 'Novo Estoque' }}</h2>
    </div>
    @if (loading()) { <app-loading-spinner /> }
    @else {
      <mat-card><mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-grid">
          <mat-form-field appearance="outline"><mat-label>Warehouse ID</mat-label><input matInput formControlName="warehouseId" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Localização</mat-label><input matInput formControlName="location" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Quantidade Mínima</mat-label><input matInput type="number" formControlName="minimumQuantity" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Quantidade Máxima</mat-label><input matInput type="number" formControlName="maximumQuantity" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Ponto de Reposição</mat-label><input matInput type="number" formControlName="reorderPoint" required /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Qtd para Reposição</mat-label><input matInput type="number" formControlName="reorderQuantity" required /></mat-form-field>
          <div class="form-actions"><button mat-button type="button" routerLink="/stocks">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">{{ isEdit ? 'Salvar' : 'Criar' }}</button></div>
        </form>
      </mat-card-content></mat-card>
    }
  `,
  styles: [`.form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; } .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; } .form-actions { grid-column: 1/-1; display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }`],
})
export class StockFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private stockService = inject(StockService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  isEdit = false; private stockId = '';
  loading = signal(true); saving = signal(false);

  form: FormGroup = this.fb.group({
    warehouseId: ['', [Validators.required, Validators.pattern('^[A-Z0-9\\-]{2,20}$')]],
    location: ['', Validators.required],
    minimumQuantity: [0, [Validators.required, Validators.min(0)]],
    maximumQuantity: [100, [Validators.required, Validators.min(0)]],
    reorderPoint: [10, [Validators.required, Validators.min(0)]],
    reorderQuantity: [50, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.stockId = this.route.snapshot.paramMap.get('id') || '';
    this.isEdit = !!this.stockId;
    if (this.isEdit) {
      this.stockService.getById(this.stockId).subscribe(s => {
        this.form.patchValue({ warehouseId: s.warehouseId, location: s.location, minimumQuantity: s.minimumQuantity, maximumQuantity: s.maximumQuantity, reorderPoint: s.reorderPoint, reorderQuantity: s.reorderQuantity });
        this.loading.set(false);
      });
    } else this.loading.set(false);
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const data = this.form.value;
    const obs = this.isEdit
      ? this.stockService.update(this.stockId, data)
      : this.stockService.create(this.route.snapshot.paramMap.get('productId')!, data);
    obs.subscribe({
      next: () => { this.snackBar.open(this.isEdit ? 'Estoque atualizado!' : 'Estoque criado!', 'OK', { duration: 3000 }); this.router.navigate(['/stocks']); },
      error: () => this.saving.set(false),
    });
  }
}
