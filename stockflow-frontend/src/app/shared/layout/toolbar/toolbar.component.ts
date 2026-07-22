import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { AuthService } from '../../../core/auth/auth.service';
import { AsyncPipe } from '@angular/common';
import { NotificationBellComponent } from '../../components/notification-bell/notification-bell.component';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [
    MatToolbarModule, MatIconModule, MatButtonModule,
    MatMenuModule, MatBadgeModule, AsyncPipe, NotificationBellComponent, MatDividerModule
  ],
  template: `
    <mat-toolbar class="toolbar">
      @if (showMenuButton) {
        <button mat-icon-button class="menu-btn" (click)="menuClick.emit()">
          <mat-icon>menu</mat-icon>
        </button>
      }
      <span class="toolbar-title">StockFlow</span>
      <span class="spacer"></span>

      <app-notification-bell />

      <button mat-icon-button [matMenuTriggerFor]="userMenu" class="user-btn">
        <mat-icon>account_circle</mat-icon>
      </button>

      <mat-menu #userMenu="matMenu" xPosition="before">
        <div class="menu-header" mat-menu-item disabled>
          <strong>{{ (auth.userProfile$ | async)?.userName }}</strong>
        </div>
        <button mat-menu-item routerLink="/profile">
          <mat-icon>person</mat-icon> Meu Perfil
        </button>
        <mat-divider />
        <button mat-menu-item (click)="auth.logout()">
          <mat-icon color="warn">logout</mat-icon> Sair
        </button>
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .toolbar {
      background: #fff; color: #1a1a2e;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
      height: 64px; padding: 0 24px;
    }
    .menu-btn { color: #1a1a2e; margin-right: 8px; }
    .toolbar-title { font-weight: 600; font-size: 18px; }
    .spacer { flex: 1; }
    .user-btn { color: #1a1a2e; }
    .menu-header { padding: 8px 16px; opacity: 1 !important; cursor: default; }
  `]
})
export class ToolbarComponent {
  protected auth = inject(AuthService);
  @Input() showMenuButton = false;
  @Output() menuClick = new EventEmitter<void>();
}
