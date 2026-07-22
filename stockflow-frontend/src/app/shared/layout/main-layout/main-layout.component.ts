import { Component, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { ToolbarComponent } from '../toolbar/toolbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterModule, MatSidenavModule, ToolbarComponent, SidebarComponent],
  template: `
    <mat-sidenav-container class="layout-container">
      <mat-sidenav
        [mode]="isMobile() ? 'over' : 'side'"
        [opened]="isMobile() ? sidenavOpen() : true"
        (closed)="sidenavOpen.set(false)"
        class="sidebar"
      >
        <app-sidebar (navClick)="onNavClick()" />
      </mat-sidenav>
      <mat-sidenav-content class="content">
        <app-toolbar (menuClick)="toggleSidenav()" [showMenuButton]="isMobile()" />
        <main class="main-content">
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    :host { display: block; height: 100dvh; }
    .layout-container { height: 100%; }
    .sidebar { width: 260px; background: #fff; border-right: 1px solid #e8eaed; }
    .content { display: flex; flex-direction: column; background: #f5f5f5; }
    .main-content { flex: 1; padding: 24px; overflow-y: auto; }
    @media (max-width: 768px) {
      .main-content { padding: 16px; }
    }
  `]
})
export class MainLayoutComponent {
  sidenavOpen = signal(false);

  isMobile(): boolean {
    return window.innerWidth <= 768;
  }

  toggleSidenav(): void {
    this.sidenavOpen.update(v => !v);
  }

  onNavClick(): void {
    if (this.isMobile()) {
      this.sidenavOpen.set(false);
    }
  }
}
