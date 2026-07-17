import { Routes } from '@angular/router';
import { AuthGuard } from '../../core/auth/auth.guard';
import { RoleGuard } from '../../core/auth/role.guard';
import { UserRole } from '../../core/models/enums';

export const PRODUCT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./product-list/product-list.component').then(m => m.ProductListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./product-form/product-form.component').then(m => m.ProductFormComponent),
    canActivate: [RoleGuard],
    data: { role: UserRole.MANAGER },
  },
  {
    path: ':id',
    loadComponent: () => import('./product-detail/product-detail.component').then(m => m.ProductDetailComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./product-form/product-form.component').then(m => m.ProductFormComponent),
    canActivate: [RoleGuard],
    data: { role: UserRole.MANAGER },
  },
];
