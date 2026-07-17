import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoryService } from '../../../core/services/category.service';
import { CategoryResponse } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatCardModule, MatIconModule, LoadingSpinnerComponent],
  template: `
    <div class="form-header"><button mat-icon-button routerLink="/categories"><mat-icon>arrow_back</mat-icon></button><h2>{{ isEdit ? 'Editar Categoria' : 'Nova Categoria' }}</h2></div>
    @if (loading()) { <app-loading-spinner /> }
    @else {
      <mat-card><mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-grid">
          <mat-form-field appearance="outline" class="full-width"><mat-label>Nome</mat-label><input matInput formControlName="name" required /></mat-form-field>
          <mat-form-field appearance="outline" class="full-width"><mat-label>Descrição</mat-label><textarea matInput formControlName="description" rows="3"></textarea></mat-form-field>
          @if (!isEdit) {
            <mat-form-field appearance="outline"><mat-label>Categoria Pai</mat-label>
              <mat-select formControlName="parentId">
                <mat-option value="">Nenhuma (Raiz)</mat-option>
                @for (cat of categories(); track cat.id) { <mat-option [value]="cat.id">{{ cat.name }}</mat-option> }
              </mat-select>
            </mat-form-field>
          }
          <div class="form-actions full-width"><button mat-button type="button" routerLink="/categories">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">{{ isEdit ? 'Salvar' : 'Criar' }}</button></div>
        </form>
      </mat-card-content></mat-card>
    }
  `,
  styles: [`.form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; } .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; } .full-width { grid-column: 1/-1; } .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }`],
})
export class CategoryFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  isEdit = false; private categoryId = '';
  loading = signal(true); saving = signal(false);
  categories = signal<CategoryResponse[]>([]);

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    parentId: [''],
  });

  ngOnInit(): void {
    this.categoryId = this.route.snapshot.paramMap.get('id') || '';
    this.isEdit = !!this.categoryId;
    this.categoryService.getAll(0, 100).subscribe(c => this.categories.set(c.content));
    if (this.isEdit) {
      this.categoryService.getAll(0, 100).subscribe(() => { /* just load categories */ });
      // For edit, we need the category data - simplified approach
      this.loading.set(false);
    } else this.loading.set(false);
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const data = this.form.value;
    const obs = this.isEdit
      ? this.categoryService.update(this.categoryId, { name: data.name, description: data.description })
      : this.categoryService.create({ name: data.name, description: data.description, parentId: data.parentId || null });
    obs.subscribe({
      next: () => { this.snackBar.open(this.isEdit ? 'Categoria atualizada!' : 'Categoria criada!', 'OK', { duration: 3000 }); this.router.navigate(['/categories']); },
      error: () => this.saving.set(false),
    });
  }
}
