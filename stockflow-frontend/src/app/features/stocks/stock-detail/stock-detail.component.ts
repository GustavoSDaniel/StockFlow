import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { PageEvent } from '@angular/material/paginator';
import { StockService } from '../../../core/services/stock.service';
import { StockResponse, InventoryMovementResponse } from '../../../core/models/domain.models';
import { Page } from '../../../core/models/page.model';
import { StockStatusIndicatorComponent } from '../../../shared/components/stock-status-indicator/stock-status-indicator.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-stock-detail',
  standalone: true,
  imports: [RouterModule, MatCardModule, MatButtonModule, MatIconModule, MatMenuModule, StockStatusIndicatorComponent, DataTableComponent, LoadingSpinnerComponent, CurrencyBrPipe, EnumLabelPipe, DatePipe],
  template: `
    <div class="detail-header">
      <button mat-icon-button routerLink="/stocks"><mat-icon>arrow_back</mat-icon></button>
      <h2>{{ stock()?.productName || 'Detalhes do Estoque' }}</h2>
      <span class="spacer"></span>
      <button mat-flat-button color="primary" [routerLink]="['/stocks', stock()?.id, 'movement']">
        <mat-icon>swap_vert</mat-icon> Movimentar
      </button>
    </div>

    @if (loading()) { <app-loading-spinner /> }
    @else if (stock()) {
      <div class="cards-grid">
        <mat-card><mat-card-content>
          <div class="stat-label">Status</div>
          <app-stock-status-indicator [status]="stock()!.stockStatus" [quantity]="stock()!.currentQuantity" [showQuantity]="true" />
        </mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Quantidade Atual</div><div class="stat-value">{{ stock()!.currentQuantity }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Mínimo</div><div class="stat-value">{{ stock()!.minimumQuantity }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Máximo</div><div class="stat-value">{{ stock()!.maximumQuantity }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Ponto Reposição</div><div class="stat-value">{{ stock()!.reorderPoint }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Qtd Reposição</div><div class="stat-value">{{ stock()!.reorderQuantity }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Warehouse</div><div class="stat-value">{{ stock()!.warehouseId }}</div></mat-card-content></mat-card>
        <mat-card><mat-card-content><div class="stat-label">Localização</div><div class="stat-value">{{ stock()!.location }}</div></mat-card-content></mat-card>
      </div>

      <h3 style="margin-top: 32px;">Histórico de Movimentações</h3>
      <app-data-table
        [data]="movements()" [columns]="movementColumns" [loading]="movLoading()"
        (onPage)="onMovementPage($event)"
      />
    }
  `,
  styles: [`
    .detail-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .detail-header h2 { margin: 0; } .spacer { flex: 1; }
    .cards-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .stat-label { font-size: 12px; color: #666; margin-bottom: 4px; }
    .stat-value { font-size: 24px; font-weight: 600; color: #1a1a2e; }
  `],
})
export class StockDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private stockService = inject(StockService);

  stock = signal<StockResponse | null>(null);
  loading = signal(true);
  movements = signal<Page<InventoryMovementResponse> | null>(null);
  movLoading = signal(false);
  private movPage = 0;

  movementColumns: ColumnDef[] = [
    { key: 'movementType', header: 'Tipo', cell: (r: any) => new EnumLabelPipe().transform(r.movementType, 'movementType') as string },
    { key: 'quantity', header: 'Qtd' },
    { key: 'quantityBefore', header: 'Antes' },
    { key: 'quantityAfter', header: 'Depois' },
    { key: 'reason', header: 'Motivo', cell: (r: any) => new EnumLabelPipe().transform(r.reason, 'movementReason') as string },
    { key: 'createdAt', header: 'Data', cell: (r: any) => new DatePipe('pt-BR').transform(r.createdAt, 'dd/MM/yyyy HH:mm') as string },
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.stockService.getById(id).pipe(finalize(() => this.loading.set(false)))
      .subscribe(s => this.stock.set(s));
    this.loadMovements(id);
  }

  private loadMovements(stockId: string): void {
    this.movLoading.set(true);
    this.stockService.getMovements(stockId, this.movPage)
      .pipe(finalize(() => this.movLoading.set(false)))
      .subscribe(p => this.movements.set(p));
  }

  onMovementPage(event: PageEvent): void {
    this.movPage = event.pageIndex;
    this.loadMovements(this.route.snapshot.paramMap.get('id')!);
  }
}