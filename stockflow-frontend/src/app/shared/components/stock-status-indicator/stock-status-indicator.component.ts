import { Component, Input } from '@angular/core';
import { StockStatus } from '../../../core/models/enums';
import { STOCK_STATUS_LABELS } from '../../../core/models/enums';

@Component({
  selector: 'app-stock-status-indicator',
  standalone: true,
  template: `
    <div class="indicator" [style.background]="color">
      <span class="dot" [style.background]="dotColor"></span>
      {{ label }}
      @if (showQuantity && quantity != null) {
        <span class="qty">({{ quantity }})</span>
      }
    </div>
  `,
  styles: [`
    .indicator { display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 500; }
    .dot { width: 8px; height: 8px; border-radius: 50%; }
    .qty { opacity: 0.7; font-weight: 400; }
  `]
})
export class StockStatusIndicatorComponent {
  @Input() status: StockStatus = StockStatus.NORMAL;
  @Input() quantity: number | null = null;
  @Input() showQuantity = false;

  private static config: Record<StockStatus, { bg: string; dot: string }> = {
    [StockStatus.OUT_OF_STOCK]: { bg: '#fee2e2', dot: '#dc2626' },
    [StockStatus.LOW]: { bg: '#fef3c7', dot: '#d97706' },
    [StockStatus.REORDER_POINT]: { bg: '#ffedd5', dot: '#ea580c' },
    [StockStatus.NORMAL]: { bg: '#dcfce7', dot: '#16a34a' },
    [StockStatus.OVER_STOCKED]: { bg: '#e0e7ff', dot: '#4f46e5' },
  };

  get color(): string {
    return StockStatusIndicatorComponent.config[this.status]?.bg ?? '#f1f5f9';
  }

  get dotColor(): string {
    return StockStatusIndicatorComponent.config[this.status]?.dot ?? '#999';
  }

  get label(): string {
    return STOCK_STATUS_LABELS[this.status] ?? this.status;
  }
}
