import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserRole } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const requiredRole: UserRole = route.data['role'];
    if (!requiredRole) return true;

    const hasRole = this.auth.hasRole(requiredRole);
    if (!hasRole) {
      this.router.navigate(['/dashboard/stocks']);
      return false;
    }
    return true;
  }
}
