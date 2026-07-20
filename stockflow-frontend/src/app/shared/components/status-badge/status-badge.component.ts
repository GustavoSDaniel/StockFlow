import { Component, Input } from '@angular/core';
import {
  PRODUCT_STATUS_LABELS, STOCK_STATUS_LABELS,
  USER_ROLE_LABELS, NOTIFICATION_PRIORITY_LABELS, NOTIFICATION_TYPE_LABELS,
} from '../../../core/models/enums';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span class="badge" [style.background]="bgColor" [style.color]="textColor">
      {{ displayLabel }}
    </span>
  `,
  styles: [`
    .badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; white-space: nowrap; }
  `]
})
export class StatusBadgeComponent {
  @Input() label = '';

  /** Mapa de tradução PT-BR. Ordem de merge: stockStatus sobrescreve notificationPriority
   *  pois as notificações já chegam pré-traduzidas do notification-list. */
  private static labelMap: Record<string, string> = {
    ...PRODUCT_STATUS_LABELS,
    ...STOCK_STATUS_LABELS,          // LOW → 'Estoque Baixo'
    ...USER_ROLE_LABELS,
    ...NOTIFICATION_TYPE_LABELS,
    ...NOTIFICATION_PRIORITY_LABELS, // LOW → 'Baixa' (só afeta se vier raw, o que não ocorre)
  };

  private static colors: Record<string, { bg: string; text: string }> = {
    // product status
    ACTIVE: { bg: '#dcfce7', text: '#166534' },
    INACTIVE: { bg: '#fee2e2', text: '#991b1b' },
    DISCONTINUED: { bg: '#f1f5f9', text: '#475569' },
    // stock status
    OUT_OF_STOCK: { bg: '#fee2e2', text: '#991b1b' },
    LOW: { bg: '#fef3c7', text: '#92400e' },
    REORDER_POINT: { bg: '#ffedd5', text: '#9a3412' },
    NORMAL: { bg: '#dcfce7', text: '#166534' },
    OVER_STOCKED: { bg: '#e0e7ff', text: '#3730a3' },
    // notification priority (raw enum values)
    MEDIUM: { bg: '#dbeafe', text: '#1e40af' },
    HIGH: { bg: '#fef3c7', text: '#92400e' },
    CRITICAL: { bg: '#fee2e2', text: '#991b1b' },
    // notification priority (pré-traduzido pelo notification-list — compatibilidade)
    Baixa: { bg: '#dbeafe', text: '#1e40af' },
    Média: { bg: '#dbeafe', text: '#1e40af' },
    Alta: { bg: '#fef3c7', text: '#92400e' },
    Crítica: { bg: '#fee2e2', text: '#991b1b' },
    // role
    ADMIN: { bg: '#f3e8ff', text: '#6b21a8' },
    MANAGER: { bg: '#dbeafe', text: '#1e40af' },
    EMPLOYEE: { bg: '#f1f5f9', text: '#475569' },
  };

  get displayLabel(): string {
    return StatusBadgeComponent.labelMap[this.label] ?? this.label;
  }

  get bgColor(): string {
    return StatusBadgeComponent.colors[this.label]?.bg ?? '#f1f5f9';
  }
  get textColor(): string {
    return StatusBadgeComponent.colors[this.label]?.text ?? '#475569';
  }
}
