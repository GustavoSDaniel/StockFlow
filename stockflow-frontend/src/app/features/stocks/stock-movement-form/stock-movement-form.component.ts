import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { StockService } from '../../../core/services/stock.service';
import { StockResponse, InventoryMovementRequest, TransferRequest } from '../../../core/models/domain.models';
import { MovementType, MOVEMENT_TYPE_LABELS, MovementReason, MOVEMENT_REASON_LABELS, VALID_REASONS_BY_TYPE } from '../../../core/models/enums';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-stock-movement-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatCardModule, MatIconModule, LoadingSpinnerComponent],
  template: `
    <div class="form-header">
      <button mat-icon-button routerLink="/stocks"><mat-icon>arrow_back</mat-icon></button>
      <h2>Movimentação de Estoque</h2>
    </div>

    @if (loading()) { <app-loading-spinner /> }
    @else {
      <mat-card>
        <mat-card-content>
          <p class="stock-info">
            <strong>{{ stock()?.productName }}</strong> |
            Warehouse: {{ stock()?.warehouseId }} |
            Quantidade atual: <strong>{{ stock()?.currentQuantity }}</strong>
          </p>

          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-grid">
            <mat-form-field appearance="outline">
              <mat-label>Tipo de Movimentação</mat-label>
              <mat-select formControlName="movementType">
                @for (mt of movementTypes; track mt.value) {
                  <mat-option [value]="mt.value">{{ mt.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Quantidade</mat-label>
              <input matInput type="number" formControlName="quantity" required min="1" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Motivo</mat-label>
              <mat-select formControlName="movementReason">
                @for (mr of availableReasons(); track mr) {
                  <mat-option [value]="mr">{{ MOVEMENT_REASON_LABELS[mr] }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            @if (isTransfer) {
              <mat-form-field appearance="outline">
                <mat-label>Warehouse Destino</mat-label>
                <input matInput formControlName="targetWarehouseId" required />
              </mat-form-field>
            }

            @if (isEntry) {
              <mat-form-field appearance="outline">
                <mat-label>Custo Unitário</mat-label>
                <input matInput type="number" formControlName="unitCost" step="0.01" />
              </mat-form-field>
            }

            <mat-form-field appearance="outline">
              <mat-label>Nº Referência</mat-label>
              <input matInput formControlName="referenceNumber" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Observação</mat-label>
              <textarea matInput formControlName="note" rows="2"></textarea>
            </mat-form-field>

            <div class="form-actions full-width">
              <button mat-button type="button" routerLink="/stocks">Cancelar</button>
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">
                Confirmar Movimentação
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`
    .form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .stock-info { padding: 12px; background: #f0f4ff; border-radius: 8px; margin-bottom: 16px; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .full-width { grid-column: 1 / -1; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    @media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } }
  `],
})
export class StockMovementFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private stockService = inject(StockService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  stock = signal<StockResponse | null>(null);
  loading = signal(true);
  saving = signal(false);
  protected MOVEMENT_REASON_LABELS = MOVEMENT_REASON_LABELS;

  movementTypes = Object.values(MovementType)
    .filter(t => t !== MovementType.TRANSFER)
    .map(v => ({ value: v, label: MOVEMENT_TYPE_LABELS[v] }));

  form: FormGroup = this.fb.group({
    movementType: [MovementType.ENTRY, Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    movementReason: ['', Validators.required],
    referenceNumber: [''],
    note: [''],
    unitCost: [0],
    targetWarehouseId: [''],
  });

  availableReasons = signal<MovementReason[]>([]);

  get isEntry(): boolean { const t = this.form.get('movementType')?.value; return t === MovementType.ENTRY || t === MovementType.RETURN; }
  get isTransfer(): boolean { return this.form.get('movementType')?.value === MovementType.TRANSFER; }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.stockService.getById(id).subscribe(s => { this.stock.set(s); this.loading.set(false); });

    this.form.get('movementType')?.valueChanges.subscribe((type: MovementType) => {
      this.availableReasons.set(VALID_REASONS_BY_TYPE[type] || []);
      this.form.get('movementReason')?.setValue('');
    });
    // Trigger initial load
    this.availableReasons.set(VALID_REASONS_BY_TYPE[MovementType.ENTRY] || []);
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const data = this.form.value;
    const stockId = this.route.snapshot.paramMap.get('id')!;
    const movementType: MovementType = data.movementType;

    const request: InventoryMovementRequest = {
      movementType, quantity: data.quantity, movementReason: data.movementReason,
      referenceNumber: data.referenceNumber, note: data.note,
      supplierId: '', customerId: '', unitCost: data.unitCost || 0,
    };

    let obs;
    switch (movementType) {
      case MovementType.ENTRY: case MovementType.RETURN: obs = this.stockService.entry(stockId, request); break;
      case MovementType.EXIT: obs = this.stockService.exit(stockId, request); break;
      case MovementType.ADJUSTMENT: obs = this.stockService.adjust(stockId, request); break;
      default: obs = this.stockService.exit(stockId, request);
    }

    obs.subscribe({
      next: () => { this.snackBar.open('Movimentação realizada!', 'OK', { duration: 3000 }); this.router.navigate(['/stocks', stockId]); },
      error: () => this.saving.set(false),
    });
  }
}
