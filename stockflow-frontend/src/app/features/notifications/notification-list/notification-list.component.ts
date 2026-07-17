import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTabsModule } from '@angular/material/tabs';
import { FormsModule } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationResponse } from '../../../core/models/domain.models';
import { NotificationPriority, NOTIFICATION_PRIORITY_LABELS, NotificationType, NOTIFICATION_TYPE_LABELS } from '../../../core/models/enums';
import { Page } from '../../../core/models/page.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule, MatSelectModule, MatFormFieldModule, MatTabsModule, FormsModule, PageHeaderComponent, DataTableComponent, StatusBadgeComponent, EnumLabelPipe, DatePipe, NgTemplateOutlet],
  template: `
    <app-page-header title="Notificações" subtitle="Alertas e notificações do sistema" />
    <mat-tab-group (selectedIndexChange)="onTabChange($event)">
      <mat-tab label="Todas">
        <app-data-table [data]="data()" [columns]="columns" [loading]="loading()" (onPage)="onPage($event)" [actionsTemplate]="actions" />
      </mat-tab>
      <mat-tab label="Não Lidas">
        <app-data-table [data]="data()" [columns]="columns" [loading]="loading()" (onPage)="onPage($event)" [actionsTemplate]="actions" />
      </mat-tab>
      <mat-tab label="Não Resolvidas">
        <app-data-table [data]="data()" [columns]="columns" [loading]="loading()" (onPage)="onPage($event)" [actionsTemplate]="actions" />
      </mat-tab>
    </mat-tab-group>
    <ng-template #actions let-row>
      <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
      <mat-menu #menu="matMenu">
        @if (!row.read) { <button mat-menu-item (click)="markRead(row)"><mat-icon>mark_email_read</mat-icon> Marcar Lida</button> }
        @if (!row.resolved) { <button mat-menu-item (click)="markResolved(row)"><mat-icon>check_circle</mat-icon> Marcar Resolvida</button> }
      </mat-menu>
    </ng-template>
  `,
})
export class NotificationListComponent implements OnInit {
  private notificationService = inject(NotificationService);
  data = signal<Page<NotificationResponse> | null>(null);
  loading = signal(false);
  private tab = 0; private page = 0;

  columns: ColumnDef[] = [
    { key: 'title', header: 'Título' },
    { key: 'productName', header: 'Produto' },
    { key: 'notificationType', header: 'Tipo', cell: (r: any) => new EnumLabelPipe().transform(r.notificationType, 'notificationType') as string },
    { key: 'notificationPriority', header: 'Prioridade' },
    { key: 'read', header: 'Lida', cell: (r: any) => r.read ? 'Sim' : 'Não' },
    { key: 'createdAt', header: 'Data', cell: (r: any) => new DatePipe('pt-BR').transform(r.createdAt, 'dd/MM/yyyy HH:mm') as string },
  ];

  ngOnInit(): void { this.loadData(); }

  onTabChange(i: number): void { this.tab = i; this.page = 0; this.loadData(); }
  onPage(e: PageEvent): void { this.page = e.pageIndex; this.loadData(); }

  private loadData(): void {
    this.loading.set(true);
    let obs;
    if (this.tab === 1) obs = this.notificationService.getUnread(this.page);
    else if (this.tab === 2) obs = this.notificationService.getUnresolved(this.page);
    else obs = this.notificationService.getAll(this.page);
    obs.pipe(finalize(() => this.loading.set(false)))
      .subscribe({ next: p => this.data.set(p), error: () => this.data.set(null) });
  }

  markRead(row: NotificationResponse): void {
    this.notificationService.markAsRead(row.id).subscribe(() => this.loadData());
  }

  markResolved(row: NotificationResponse): void {
    this.notificationService.markAsResolved(row.id).subscribe(() => this.loadData());
  }
}