import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { RoleGuard } from './core/auth/role.guard';
import { UserRole } from './core/models/enums';
import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard/stocks', pathMatch: 'full' },

  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        children: [
          {
            path: 'overview',
            loadComponent: () => import('./features/dashboard/overview/overview.component').then(m => m.OverviewDashboardComponent),
            canActivate: [RoleGuard],
            data: { role: UserRole.ADMIN },
          },
          {
            path: 'stocks',
            loadComponent: () => import('./features/dashboard/stocks/stock-dashboard.component').then(m => m.StockDashboardComponent),
          },
          {
            path: 'movements',
            loadComponent: () => import('./features/dashboard/movements/movements-dashboard.component').then(m => m.MovementsDashboardComponent),
          },
          {
            path: 'suppliers',
            loadComponent: () => import('./features/dashboard/suppliers/supplier-dashboard.component').then(m => m.SupplierDashboardComponent),
            canActivate: [RoleGuard],
            data: { role: UserRole.MANAGER },
          },
        ],
      },
      {
        path: 'products',
        loadChildren: () => import('./features/products/products.routes').then(m => m.PRODUCT_ROUTES),
      },
      {
        path: 'stocks',
        loadChildren: () => import('./features/stocks/stocks.routes').then(m => m.STOCK_ROUTES),
      },
      {
        path: 'categories',
        loadChildren: () => import('./features/categories/categories.routes').then(m => m.CATEGORY_ROUTES),
      },
      {
        path: 'suppliers',
        loadChildren: () => import('./features/suppliers/suppliers.routes').then(m => m.SUPPLIER_ROUTES),
      },
      {
        path: 'users',
        loadChildren: () => import('./features/users/users.routes').then(m => m.USER_ROUTES),
        canActivate: [RoleGuard],
        data: { role: UserRole.MANAGER },
      },
      {
        path: 'notifications',
        loadChildren: () => import('./features/notifications/notifications.routes').then(m => m.NOTIFICATION_ROUTES),
        canActivate: [RoleGuard],
        data: { role: UserRole.MANAGER },
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/users/user-profile/user-profile.component').then(m => m.UserProfileComponent),
      },
    ],
  },
  { path: '**', redirectTo: '/dashboard/stocks' },
];
