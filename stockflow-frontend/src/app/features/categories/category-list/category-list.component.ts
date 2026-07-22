import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { NestedTreeControl } from '@angular/cdk/tree';
import { CategoryService } from '../../../core/services/category.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../core/models/enums';
import { CategoryResponse } from '../../../core/models/domain.models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [RouterModule, MatButtonModule, MatIconModule, MatMenuModule, MatTooltipModule, MatTreeModule, PageHeaderComponent, StatusBadgeComponent, LoadingSpinnerComponent],
  template: `
    <app-page-header title="Categorias" subtitle="Gerencie a hierarquia de categorias" createLabel="Nova Categoria" createRoute="/categories/new" requiredRole="MANAGER" />
    @if (loading()) { <app-loading-spinner /> }
    @else {
      <div class="category-container">
      <mat-tree [dataSource]="dataSource" [treeControl]="treeControl" class="category-tree">
        <mat-tree-node *matTreeNodeDef="let node" matTreeNodeToggle>
          <mat-icon class="tree-icon">folder</mat-icon>
          <span class="tree-name">{{ node.name }}</span>
          @if (!node.active) { <app-status-badge label="INACTIVE" /> }
          <span class="spacer"></span>
          @if (auth.hasRole(UserRole.MANAGER)) {
            <button mat-icon-button [routerLink]="['/categories', node.id, 'edit']"><mat-icon>edit</mat-icon></button>
            @if (node.active) {
              <button mat-icon-button (click)="onDisable(node)" matTooltip="Desativar categoria"><mat-icon>toggle_off</mat-icon></button>
            } @else {
              <button mat-icon-button (click)="onActivate(node)" matTooltip="Ativar categoria"><mat-icon>toggle_on</mat-icon></button>
            }
          }
          @if (auth.hasRole(UserRole.ADMIN)) {
            <button mat-icon-button (click)="onDelete(node)"><mat-icon color="warn">delete</mat-icon></button>
          }
        </mat-tree-node>
        <mat-nested-tree-node *matTreeNodeDef="let node; when: hasChild">
          <div class="mat-tree-node" matTreeNodeToggle>
            <mat-icon class="tree-icon">{{ treeControl.isExpanded(node) ? 'expand_more' : 'chevron_right' }}</mat-icon>
            <mat-icon class="tree-icon">folder</mat-icon>
            <span class="tree-name">{{ node.name }}</span>
            @if (!node.active) { <app-status-badge label="INACTIVE" /> }
            <span class="spacer"></span>
            @if (auth.hasRole(UserRole.MANAGER)) {
              <button mat-icon-button [routerLink]="['/categories', node.id, 'edit']"><mat-icon>edit</mat-icon></button>
              @if (node.active) {
                <button mat-icon-button (click)="onDisable(node)" matTooltip="Desativar categoria"><mat-icon>toggle_off</mat-icon></button>
              } @else {
                <button mat-icon-button (click)="onActivate(node)" matTooltip="Ativar categoria"><mat-icon>toggle_on</mat-icon></button>
              }
            }
            @if (auth.hasRole(UserRole.ADMIN)) {
              <button mat-icon-button (click)="onDelete(node)"><mat-icon color="warn">delete</mat-icon></button>
            }
          </div>
          <div [class.example-tree-invisible]="!treeControl.isExpanded(node)">
            <ng-container matTreeNodeOutlet />
          </div>
        </mat-nested-tree-node>
      </mat-tree>
      </div>
    }
  `,
  styles: [`
    .category-container { background: #fff; border-radius: 8px; padding: 8px 16px; border: 1px solid #e8eaed; }
    .category-tree { background: transparent; }
    .mat-tree-node { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border-radius: 6px; margin: 2px 0; }
    .mat-tree-node:hover { background: #f0f0f0; }
    .mat-nested-tree-node > div > .mat-tree-node { margin-left: 0; }
    .mat-nested-tree-node .mat-nested-tree-node .mat-tree-node { padding-left: 24px; }
    .tree-icon { color: #7c3aed; font-size: 20px; width: 20px; height: 20px; }
    .tree-name { font-weight: 500; }
    .spacer { flex: 1; }
    .example-tree-invisible { display: none; }
  `],
})
export class CategoryListComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;

  treeControl = new NestedTreeControl<CategoryResponse>(node => node.subCategories);
  dataSource = new MatTreeNestedDataSource<CategoryResponse>();
  loading = signal(true);

  hasChild = (_: number, node: CategoryResponse) => node.subCategories && node.subCategories.length > 0;

  ngOnInit(): void {
    this.categoryService.getAll(0, 100).pipe(finalize(() => this.loading.set(false)))
      .subscribe(page => this.dataSource.data = page.content);
  }

  onActivate(node: CategoryResponse): void {
    this.categoryService.activate(node.id).subscribe(() => {
      this.snackBar.open('Categoria ativada!', 'OK', { duration: 3000 });
      this.ngOnInit();
    });
  }

  onDisable(node: CategoryResponse): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: 'Desativar Categoria', message: `Desativar "${node.name}"?` } })
      .afterClosed().subscribe(confirmed => {
        if (confirmed) this.categoryService.disable(node.id).subscribe(() => {
          this.snackBar.open('Categoria desativada!', 'OK', { duration: 3000 });
          this.ngOnInit();
        });
      });
  }

  onDelete(node: CategoryResponse): void {
    this.dialog.open(ConfirmDialogComponent, { data: { title: 'Excluir Categoria', message: 'Excluir "' + node.name + '"?' } })
      .afterClosed().subscribe(confirmed => {
        if (confirmed) this.categoryService.delete(node.id).subscribe(() => { this.snackBar.open('Categoria excluída!', 'OK', { duration: 3000 }); this.ngOnInit(); });
      });
  }
}
