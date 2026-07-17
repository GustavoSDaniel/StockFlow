import { Routes } from '@angular/router';
import { RoleGuard } from '../../core/auth/role.guard';
import { UserRole } from '../../core/models/enums';

export const SUPPLIER_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./supplier-list/supplier-list.component').then(m => m.SupplierListComponent) },
  {
    path: 'new',
    loadComponent: () => import('./supplier-form/supplier-form.component').then(m => m.SupplierFormComponent),
    canActivate: [RoleGuard], data: { role: UserRole.MANAGER },
  },
  { path: ':id', loadComponent: () => import('./supplier-detail/supplier-detail.component').then(m => m.SupplierDetailComponent) },
  {
    path: ':id/edit',
    loadComponent: () => import('./supplier-form/supplier-form.component').then(m => m.SupplierFormComponent),
    canActivate: [RoleGuard], data: { role: UserRole.MANAGER },
  },
];
