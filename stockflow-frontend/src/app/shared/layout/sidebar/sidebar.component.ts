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
    :host { display: flex; flex-direction: column; height: 100%; color: #fff; }
    .sidebar-header {
      display: flex; align-items: center; gap: 12px; padding: 20px 16px;
    }
    .logo-icon { font-size: 32px; width: 32px; height: 32px; color: #7c3aed; }
    .logo-text { font-size: 22px; font-weight: 700; letter-spacing: -0.5px; color: #fff; }
    mat-divider { background: rgba(255,255,255,0.1); }

    .nav-list { padding: 8px 0; }
    .nav-item {
      color: rgba(255,255,255,0.7) !important;
      border-radius: 0;
      margin: 2px 8px;
      border-radius: 8px;
      &:hover { background: rgba(255,255,255,0.08); color: #fff !important; }
    }
    .active-link {
      background: rgba(124, 58, 237, 0.2) !important;
      color: #a78bfa !important;
      mat-icon { color: #a78bfa; }
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
