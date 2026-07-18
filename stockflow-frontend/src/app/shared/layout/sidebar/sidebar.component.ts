import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/models/enums';
import { AsyncPipe } from '@angular/common';
import { map } from 'rxjs/operators';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: UserRole[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, MatListModule, MatIconModule, MatDividerModule, AsyncPipe],
  template: `
    <div class="sidebar-header">
      <mat-icon class="logo-icon">inventory_2</mat-icon>
      <span class="logo-text">StockFlow</span>
    </div>
    <mat-divider />
    <mat-nav-list class="nav-list">
      @for (item of visibleItems$ | async; track item.route) {
        <a mat-list-item [routerLink]="item.route" routerLinkActive="active-link" class="nav-item">
          <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
          <span matListItemTitle>{{ item.label }}</span>
        </a>
      }
    </mat-nav-list>
  `,
  styles: [`
    :host {
      display: flex; flex-direction: column; height: 100%;
      background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
      color: #1a1a2e;
    }
    .sidebar-header {
      display: flex; align-items: center; gap: 12px;
      padding: 24px 20px 20px;
    }
    .logo-icon {
      font-size: 34px; width: 34px; height: 34px;
      color: #7c3aed;
      filter: drop-shadow(0 2px 8px rgba(124, 58, 237, 0.3));
    }
    .logo-text {
      font-size: 22px; font-weight: 700;
      letter-spacing: -0.5px;
      color: #1a1a2e;
    }
    mat-divider {
      background: #e8eaed;
      margin: 0 12px;
    }

    .nav-list { padding: 12px 8px; }
    .nav-item {
      color: #5f6368 !important;
      margin: 2px 8px;
      border-radius: 10px;
      font-weight: 500;
      font-size: 14px;
      transition: all 0.15s ease;
      &:hover {
        background: #f1f3f4;
        color: #1a1a2e !important;
      }
    }
    .active-link {
      background: rgba(124, 58, 237, 0.08) !important;
      color: #7c3aed !important;
      font-weight: 600;
      mat-icon { color: #7c3aed; }
    }
  `]
})
export class SidebarComponent {
  private auth = inject(AuthService);

  private allItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard/stocks' },
    { label: 'Visão Geral', icon: 'bar_chart', route: '/dashboard/overview', roles: [UserRole.ADMIN] },
    { label: 'Movimentações', icon: 'swap_vert', route: '/dashboard/movements' },
    { label: 'Fornecedores (Dash)', icon: 'local_shipping', route: '/dashboard/suppliers', roles: [UserRole.MANAGER, UserRole.ADMIN] },
    { label: 'Produtos', icon: 'category', route: '/products' },
    { label: 'Estoque', icon: 'warehouse', route: '/stocks' },
    { label: 'Categorias', icon: 'folder', route: '/categories' },
    { label: 'Fornecedores', icon: 'business', route: '/suppliers' },
    { label: 'Usuários', icon: 'people', route: '/users', roles: [UserRole.MANAGER, UserRole.ADMIN] },
    { label: 'Notificações', icon: 'notifications', route: '/notifications', roles: [UserRole.MANAGER, UserRole.ADMIN] },
  ];

  visibleItems$ = this.auth.userProfile$.pipe(
    map(profile => {
      if (!profile) return [];
      return this.allItems.filter(item => {
        if (!item.roles || item.roles.length === 0) return true;
        return item.roles.some(r => profile.roles.includes(r));
      });
    })
  );
}
