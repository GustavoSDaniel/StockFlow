import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { Page } from '../../../core/models/page.model';

export interface ColumnDef {
  key: string;
  header: string;
  sortable?: boolean;
  cell?: (item: any) => string;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatIconModule, MatButtonModule, MatMenuModule,
    MatProgressSpinnerModule, DatePipe, NgTemplateOutlet,
  ],
  template: `
    <div class="table-container">
      @if (loading) {
        <div class="loading-overlay">
          <mat-spinner diameter="40" />
        </div>
      }

      <table mat-table [dataSource]="data?.content ?? []" matSort (matSortChange)="onSort.emit($event)">
        @for (col of columns; track col.key) {
          <ng-container [matColumnDef]="col.key">
            <th mat-header-cell *matHeaderCellDef [mat-sort-header]="col.sortable !== false ? col.key : ''">
              {{ col.header }}
            </th>
            <td mat-cell *matCellDef="let row">
              @if (col.cell) {
                {{ col.cell(row) }}
              } @else {
                {{ row[col.key] }}
              }
            </td>
          </ng-container>
        }

        @if (actionsTemplate) {
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Ações</th>
            <td mat-cell *matCellDef="let row">
              <ng-container *ngTemplateOutlet="actionsTemplate; context: { $implicit: row }" />
            </td>
          </ng-container>
        }

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

        @if (!data?.content?.length && !loading) {
          <tr class="mat-row">
            <td class="mat-cell empty-cell" [attr.colspan]="displayedColumns.length">
              Nenhum registro encontrado.
            </td>
          </tr>
        }
      </table>

      <mat-paginator
        [length]="data?.totalElements ?? 0"
        [pageSize]="data?.size ?? 10"
        [pageIndex]="data?.number ?? 0"
        [pageSizeOptions]="[5, 10, 25, 50]"
        (page)="onPage.emit($event)"
        showFirstLastButtons
      />
    </div>
  `,
  styles: [`
    .table-container { position: relative; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .loading-overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.7); z-index: 2; }
    table { width: 100%; }
    .mat-mdc-row:hover { background: #f8f9fa; }
    .empty-cell { text-align: center; padding: 48px 16px; color: #999; }
  `]
})
export class DataTableComponent {
  @Input() data: Page<any> | null = null;
  @Input() columns: ColumnDef[] = [];
  @Input() actionsTemplate: any = null;
  @Input() loading = false;

  @Output() onPage = new EventEmitter<PageEvent>();
  @Output() onSort = new EventEmitter<Sort>();

  get displayedColumns(): string[] {
    const cols = this.columns.map(c => c.key);
    if (this.actionsTemplate) cols.push('actions');
    return cols;
  }
}