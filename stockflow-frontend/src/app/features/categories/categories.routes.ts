import { Routes } from '@angular/router';
import { RoleGuard } from '../../core/auth/role.guard';
import { UserRole } from '../../core/models/enums';

export const CATEGORY_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./category-list/category-list.component').then(m => m.CategoryListComponent) },
  {
    path: 'new',
    loadComponent: () => import('./category-form/category-form.component').then(m => m.CategoryFormComponent),
    canActivate: [RoleGuard], data: { role: UserRole.MANAGER },
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./category-form/category-form.component').then(m => m.CategoryFormComponent),
    canActivate: [RoleGuard], data: { role: UserRole.MANAGER },
  },
];
