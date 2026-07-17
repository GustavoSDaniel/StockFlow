import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

export interface FilterField {
  key: string;
  label: string;
  type: 'text' | 'select';
  options?: { value: string; label: string }[];
}

@Component({
  selector: 'app-filter-bar',
  standalone: true,
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatButtonModule],
  template: `
    <div class="filter-bar">
      <mat-form-field appearance="outline" class="search-field">
        <mat-icon matPrefix>search</mat-icon>
        <input matInput [placeholder]="searchPlaceholder" [(ngModel)]="searchValue" (keyup.enter)="onSearch.emit(searchValue)" />
        @if (searchValue) {
          <button matSuffix mat-icon-button (click)="searchValue = ''; onSearch.emit('')">
            <mat-icon>close</mat-icon>
          </button>
        }
      </mat-form-field>

      @for (field of filters; track field.key) {
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>{{ field.label }}</mat-label>
          @if (field.type === 'select') {
            <mat-select [(ngModel)]="filterValues[field.key]" (selectionChange)="onFilterChange.emit(filterValues)">
              <mat-option value="">Todos</mat-option>
              @for (opt of field.options; track opt.value) {
                <mat-option [value]="opt.value">{{ opt.label }}</mat-option>
              }
            </mat-select>
          } @else {
            <input matInput [(ngModel)]="filterValues[field.key]" (keyup.enter)="onFilterChange.emit(filterValues)" />
          }
        </mat-form-field>
      }
    </div>
  `,
  styles: [`
    .filter-bar { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
    .search-field { flex: 1; min-width: 250px; }
    .filter-field { min-width: 180px; }
  `]
})
export class FilterBarComponent {
  @Input() searchPlaceholder = 'Buscar...';
  @Input() filters: FilterField[] = [];
  @Output() onSearch = new EventEmitter<string>();
  @Output() onFilterChange = new EventEmitter<Record<string, string>>();

  searchValue = '';
  filterValues: Record<string, string> = {};
}
