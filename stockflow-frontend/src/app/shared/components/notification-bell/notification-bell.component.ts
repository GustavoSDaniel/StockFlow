import { Component, inject, OnInit, signal, HostListener, ElementRef } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterModule } from '@angular/router';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole, NOTIFICATION_PRIORITY_LABELS } from '../../../core/models/enums';
import { NotificationResponse } from '../../../core/models/domain.models';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [
    MatIconModule, MatBadgeModule, MatButtonModule, MatDividerModule,
    MatProgressSpinnerModule, RouterModule, DatePipe,
  ],
  template: `
    @if (enabled) {
      <div class="bell-container" #bellContainer>
        <button mat-icon-button
          [matBadge]="unreadCount"
          matBadgeColor="warn"
          [matBadgeHidden]="unreadCount === 0"
          (click)="toggleDropdown()">
          <mat-icon>notifications</mat-icon>
        </button>

        @if (isDropdownOpen()) {
          <div class="notification-dropdown" (click)="$event.stopPropagation()">
            <div class="dropdown-header">
              <strong>Notificações</strong>
              @if (unreadCount > 0) {
                <span class="unread-badge">{{ unreadCount }} não lida{{ unreadCount > 1 ? 's' : '' }}</span>
              }
            </div>

            <mat-divider />

            <div class="dropdown-body">
              @if (previewLoading()) {
                <div class="spinner-container"><mat-spinner diameter="30" /></div>
              } @else if (previewNotifications().length === 0) {
                <p class="empty-text">Nenhuma notificação recente.</p>
              } @else {
                @for (n of previewNotifications(); track n.id) {
                  <div class="notification-item" [class.unread]="!n.read">
                    <div class="notif-header">
                      <span class="notif-title">{{ n.title }}</span>
                      <span class="notif-priority" [class]="'priority-' + n.notificationPriority.toLowerCase()">
                        {{ translatePriority(n.notificationPriority) }}
                      </span>
                    </div>
                    <span class="notif-product">{{ n.productName }}</span>
                    <span class="notif-time">{{ n.createdAt | date:'dd/MM/yyyy HH:mm' }}</span>
                  </div>
                }
              }
            </div>

            <mat-divider />

            <div class="dropdown-footer">
              <button mat-button color="primary" (click)="goToNotifications()">
                Ver todas as notificações
              </button>
            </div>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .bell-container { position: relative; display: inline-block; }

    .notification-dropdown {
      position: absolute; top: 100%; right: 0;
      width: 380px; max-height: 480px;
      background: #fff; border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.12);
      z-index: 1000; overflow: hidden;
    }

    .dropdown-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 14px 16px;
    }
    .dropdown-header strong { font-size: 15px; color: #1a1a2e; }
    .unread-badge {
      background: #fef2f2; color: #dc2626;
      padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 500;
    }

    .dropdown-body { max-height: 360px; overflow-y: auto; }

    .spinner-container { display: flex; justify-content: center; padding: 32px 0; }
    .empty-text { text-align: center; color: #9ca3af; padding: 32px 0; margin: 0; font-size: 14px; }

    .notification-item {
      padding: 12px 16px; border-bottom: 1px solid #f1f5f9;
      display: flex; flex-direction: column; gap: 4px;
      cursor: pointer; transition: background 0.15s;
    }
    .notification-item:hover { background: #f8fafc; }
    .notification-item.unread { background: #f0f4ff; }
    .notification-item.unread:hover { background: #e8edfa; }

    .notif-header { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
    .notif-title { font-size: 13px; font-weight: 500; color: #1a1a2e; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; }
    .notif-product { font-size: 12px; color: #6b7280; }
    .notif-time { font-size: 11px; color: #9ca3af; }

    .notif-priority { font-size: 10px; font-weight: 600; padding: 2px 8px; border-radius: 8px; white-space: nowrap; text-transform: uppercase; }
    .priority-critical { background: #fee2e2; color: #991b1b; }
    .priority-high { background: #fef3c7; color: #92400e; }
    .priority-medium { background: #dbeafe; color: #1e40af; }
    .priority-low { background: #f1f5f9; color: #475569; }

    .dropdown-footer { padding: 8px; display: flex; justify-content: center; }
  `],
})
export class NotificationBellComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private elementRef = inject(ElementRef);

  unreadCount = 0;
  enabled = false;

  isDropdownOpen = signal(false);
  previewLoading = signal(false);
  previewNotifications = signal<NotificationResponse[]>([]);

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    if (this.isDropdownOpen() && !this.elementRef.nativeElement.contains(event.target)) {
      this.isDropdownOpen.set(false);
    }
  }

  ngOnInit(): void {
    this.enabled = this.auth.hasRole(UserRole.MANAGER);
    if (this.enabled) {
      this.loadUnreadCount();
    }
  }

  toggleDropdown(): void {
    const wasOpen = this.isDropdownOpen();
    this.isDropdownOpen.set(!wasOpen);
    if (!wasOpen) {
      this.loadPreview();
    }
  }

  private loadUnreadCount(): void {
    this.notificationService.getUnread(0, 1).subscribe({
      next: page => this.unreadCount = page.totalElements,
    });
  }

  private loadPreview(): void {
    this.previewLoading.set(true);
    this.notificationService.getAll(0, 5).subscribe({
      next: page => {
        this.previewNotifications.set(page.content);
        this.previewLoading.set(false);
      },
      error: () => this.previewLoading.set(false),
    });
  }

  goToNotifications(): void {
    this.isDropdownOpen.set(false);
    this.router.navigate(['/notifications']);
  }

  /** Traduz prioridade para português */
  translatePriority(priority: string): string {
    return NOTIFICATION_PRIORITY_LABELS[priority as keyof typeof NOTIFICATION_PRIORITY_LABELS] ?? priority;
  }
}
