import { Component } from '@angular/core';
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
      <mat-sidenav mode="side" opened class="sidebar">
        <app-sidebar />
      </mat-sidenav>
      <mat-sidenav-content class="content">
        <app-toolbar />
        <main class="main-content">
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    :host { display: block; height: 100dvh; }
    .layout-container { height: 100%; }
    .sidebar { width: 260px; background: #1a1a2e; border-right: none; }
    .content { display: flex; flex-direction: column; background: #f5f5f5; }
    .main-content { flex: 1; padding: 24px; overflow-y: auto; }
  `]
})
export class MainLayoutComponent {}
