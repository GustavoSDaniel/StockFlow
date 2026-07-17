import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span class="badge" [style.background]="bgColor" [style.color]="textColor">
      {{ label }}
    </span>
  `,
  styles: [`
    .badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; white-space: nowrap; }
  `]
})
export class StatusBadgeComponent {
  @Input() label = '';

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
    // notification priority
    MEDIUM: { bg: '#dbeafe', text: '#1e40af' },
    HIGH: { bg: '#fef3c7', text: '#92400e' },
    CRITICAL: { bg: '#fee2e2', text: '#991b1b' },
    // role
    ADMIN: { bg: '#f3e8ff', text: '#6b21a8' },
    MANAGER: { bg: '#dbeafe', text: '#1e40af' },
    EMPLOYEE: { bg: '#f1f5f9', text: '#475569' },
  };

  get bgColor(): string {
    return StatusBadgeComponent.colors[this.label]?.bg ?? '#f1f5f9';
  }
  get textColor(): string {
    return StatusBadgeComponent.colors[this.label]?.text ?? '#475569';
  }
}
