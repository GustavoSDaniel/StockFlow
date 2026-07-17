import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="empty-state">
      <mat-icon class="empty-icon">{{ icon }}</mat-icon>
      <h3>{{ title }}</h3>
      @if (description) { <p>{{ description }}</p> }
    </div>
  `,
  styles: [`
    .empty-state { display: flex; flex-direction: column; align-items: center; padding: 64px 16px; color: #999; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; margin-bottom: 16px; opacity: 0.5; }
    h3 { margin: 0 0 8px; color: #666; font-weight: 500; }
    p { margin: 0; font-size: 14px; }
  `]
})
export class EmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() title = 'Nenhum registro encontrado';
  @Input() description = '';
}
