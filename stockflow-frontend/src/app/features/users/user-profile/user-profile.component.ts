import { Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/auth/auth.service';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, EnumLabelPipe, AsyncPipe],
  template: `
    <h2>Meu Perfil</h2>
    <mat-card>
      <mat-card-content>
        @if (auth.userProfile$ | async; as profile) {
          <div class="profile-grid">
            <div><strong>Nome:</strong> {{ profile.userName }}</div>
            <div><strong>Email:</strong> {{ profile.email }}</div>
            <div><strong>Roles:</strong> {{ profile.roles.join(', ') | enumLabel:'userRole' }}</div>
          </div>
        }
      </mat-card-content>
      <mat-card-actions>
        <button mat-stroked-button color="warn" (click)="auth.logout()"><mat-icon>logout</mat-icon> Sair</button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }`],
})
export class UserProfileComponent {
  protected auth = inject(AuthService);
}
