import { Component, inject, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/models/enums';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [MatIconModule, MatBadgeModule, MatButtonModule, RouterModule],
  template: `
    @if (enabled) {
      <button mat-icon-button [routerLink]="'/notifications'" [matBadge]="unreadCount" matBadgeColor="warn" matBadgeHidden="{{ unreadCount === 0 }}">
        <mat-icon>notifications</mat-icon>
      </button>
    }
  `,
})
export class NotificationBellComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private auth = inject(AuthService);

  unreadCount = 0;
  enabled = false;

  ngOnInit(): void {
    this.enabled = this.auth.hasRole(UserRole.MANAGER);
    if (this.enabled) {
      this.loadUnreadCount();
    }
  }

  private loadUnreadCount(): void {
    this.notificationService.getUnread(0, 1).subscribe({
      next: page => this.unreadCount = page.totalElements,
    });
  }
}
