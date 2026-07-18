import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole, USER_ROLE_LABELS } from '../../../core/models/enums';
import { UserResponse } from '../../../core/models/domain.models';
import { Page } from '../../../core/models/page.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { DataTableComponent, ColumnDef } from '../../../shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule, MatSelectModule, MatFormFieldModule, FormsModule, PageHeaderComponent, DataTableComponent, StatusBadgeComponent, EnumLabelPipe, DatePipe, NgTemplateOutlet],
  template: `
    <app-page-header title="Usuários" subtitle="Gerencie os usuários do sistema" />
    <app-data-table [data]="data()" [columns]="columns" [loading]="loading()" (onPage)="onPageChange($event)" [actionsTemplate]="actionsTemplate" />
    <ng-template #actionsTemplate let-row>
      <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
      <mat-menu #menu="matMenu">
        @if (auth.hasRole(UserRole.MANAGER) && row.active) {
          <button mat-menu-item (click)="onPromote(row)"><mat-icon>arrow_upward</mat-icon> Promover</button>
          <button mat-menu-item (click)="onDisable(row)"><mat-icon color="warn">block</mat-icon> Desativar</button>
        } @else if (auth.hasRole(UserRole.MANAGER) && !row.active) {
          <button mat-menu-item (click)="onActivate(row)"><mat-icon color="primary">check_circle</mat-icon> Ativar</button>
        }
        @if (auth.hasRole(UserRole.ADMIN)) {
          <button mat-menu-item (click)="onDelete(row)"><mat-icon color="warn">delete</mat-icon> Excluir</button>
        }
      </mat-menu>
    </ng-template>
  `,
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;
  data = signal<Page<UserResponse> | null>(null);
  loading = signal(false);
  private page = 0;

  columns: ColumnDef[] = [
    { key: 'userName', header: 'Usuário', sortable: true },
    { key: 'role', header: 'Role', cell: (r: any) => new EnumLabelPipe().transform(r.role, 'userRole') as string },
    { key: 'active', header: 'Status', cell: (r: any) => r.active ? 'Ativo' : 'Inativo' },
    { key: 'createdAt', header: 'Criado em', cell: (r: any) => new DatePipe('pt-BR').transform(r.createdAt, 'shortDate') as string },
  ];

  ngOnInit(): void { this.loadData(); }

  private loadData(): void {
    this.loading.set(true);
    this.userService.getAll(this.page).pipe(finalize(() => this.loading.set(false)))
      .subscribe({ next: p => this.data.set(p), error: () => this.data.set(null) });
  }

  onPageChange(event: PageEvent): void { this.page = event.pageIndex; this.loadData(); }

  onPromote(row: UserResponse): void {
    const newRole = row.role === UserRole.EMPLOYEE ? UserRole.MANAGER : UserRole.ADMIN;
    this.userService.promote(row.id, newRole).subscribe(() => { this.snackBar.open('Usuário promovido!', 'OK', { duration: 3000 }); this.loadData(); });
  }

  onActivate(row: UserResponse): void {
    this.userService.activate(row.id).subscribe(() => { this.snackBar.open('Usuário ativado!', 'OK', { duration: 3000 }); this.loadData(); });
  }

  onDisable(row: UserResponse): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: 'Desativar Usuário', message: 'Desativar "' + row.userName + '"?', confirmLabel: 'Desativar' } })
      .afterClosed().subscribe(c => { if (c) this.userService.disable(row.id).subscribe(() => { this.snackBar.open('Usuário desativado!', 'OK', { duration: 3000 }); this.loadData(); }); });
  }

  onDelete(row: UserResponse): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: 'Excluir Usuário', message: 'Excluir "' + row.userName + '" permanentemente?' } })
      .afterClosed().subscribe(c => { if (c) this.userService.delete(row.id).subscribe(() => { this.snackBar.open('Usuário excluído!', 'OK', { duration: 3000 }); this.loadData(); }); });
  }
}