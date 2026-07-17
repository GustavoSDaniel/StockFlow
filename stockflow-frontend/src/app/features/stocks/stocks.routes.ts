import { Routes } from '@angular/router';
import { AuthGuard } from '../../core/auth/auth.guard';
import { RoleGuard } from '../../core/auth/role.guard';
import { UserRole } from '../../core/models/enums';

export const STOCK_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./stock-list/stock-list.component').then(m => m.StockListComponent) },
  {
    path: 'new/:productId',
    loadComponent: () => import('./stock-form/stock-form.component').then(m => m.StockFormComponent),
    canActivate: [RoleGuard], data: { role: UserRole.MANAGER },
  },
  { path: ':id', loadComponent: () => import('./stock-detail/stock-detail.component').then(m => m.StockDetailComponent) },
  {
    path: ':id/edit',
    loadComponent: () => import('./stock-form/stock-form.component').then(m => m.StockFormComponent),
    canActivate: [RoleGuard], data: { role: UserRole.MANAGER },
  },
  {
    path: ':id/movement',
    loadComponent: () => import('./stock-movement-form/stock-movement-form.component').then(m => m.StockMovementFormComponent),
  },
];
