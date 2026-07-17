import { Component, Input, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [RouterModule, MatIconModule, MatButtonModule],
  template: `
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">{{ title }}</h1>
        @if (subtitle) { <p class="page-subtitle">{{ subtitle }}</p> }
      </div>
      <div class="header-actions">
        @if (createLabel && createRoute && canCreate) {
          <button mat-flat-button color="primary" [routerLink]="createRoute">
            <mat-icon>add</mat-icon> {{ createLabel }}
          </button>
        }
        <ng-content />
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      display: flex; justify-content: space-between; align-items: flex-start;
      margin-bottom: 24px;
    }
    .page-title { font-size: 24px; font-weight: 600; margin: 0; color: #1a1a2e; }
    .page-subtitle { margin: 4px 0 0; color: #666; font-size: 14px; }
    .header-actions { display: flex; gap: 8px; align-items: center; }
  `]
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() createLabel = '';
  @Input() createRoute = '';
  @Input() requiredRole?: string;

  private auth = inject(AuthService);

  get canCreate(): boolean {
    if (!this.requiredRole) return true;
    return this.auth.hasRole(this.requiredRole as any);
  }
}
