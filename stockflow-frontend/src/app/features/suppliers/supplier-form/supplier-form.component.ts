import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SupplierService } from '../../../core/services/supplier.service';
import { StateUF, STATE_UF_LABELS } from '../../../core/models/enums';
import { SupplierRequest, SupplierContactRequest, AddressRequest } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-supplier-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatCardModule, MatIconModule, LoadingSpinnerComponent],
  template: `
    <div class="form-header"><button mat-icon-button routerLink="/suppliers"><mat-icon>arrow_back</mat-icon></button><h2>{{ isEdit ? 'Editar Fornecedor' : 'Novo Fornecedor' }}</h2></div>
    @if (loading()) { <app-loading-spinner /> }
    @else {
      <mat-card><mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <h3>Dados do Fornecedor</h3>
          <div class="form-grid">
            <mat-form-field appearance="outline"><mat-label>Razão Social</mat-label><input matInput formControlName="name" required /></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>CNPJ (14 dígitos)</mat-label><input matInput formControlName="cnpj" required maxlength="14" /></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Nome Fantasia</mat-label><input matInput formControlName="tradeName" required /></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Website</mat-label><input matInput formControlName="website" /></mat-form-field>
            <mat-form-field appearance="outline"><mat-label>Pedido Mínimo (R$)</mat-label><input matInput type="number" formControlName="minOrderValue" /></mat-form-field>
            <mat-form-field appearance="outline" class="full-width"><mat-label>Observações</mat-label><textarea matInput formControlName="notes" rows="3"></textarea></mat-form-field>
          </div>

          <h3>Contatos</h3>
          <div formArrayName="contacts">
            @for (contact of contacts.controls; track i; let i = $index) {
              <div [formGroupName]="i" class="inline-form">
                <mat-form-field appearance="outline"><mat-label>Nome</mat-label><input matInput formControlName="contactName" required /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Email</mat-label><input matInput formControlName="email" required type="email" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Telefone</mat-label><input matInput formControlName="phoneNumber" required /></mat-form-field>
                <button mat-icon-button type="button" color="warn" (click)="removeContact(i)"><mat-icon>remove_circle</mat-icon></button>
              </div>
            }
            <button mat-stroked-button type="button" (click)="addContact()"><mat-icon>add</mat-icon> Adicionar Contato</button>
          </div>

          <h3>Endereços</h3>
          <div formArrayName="addresses">
            @for (addr of addresses.controls; track i; let i = $index) {
              <div [formGroupName]="i" class="form-grid">
                <mat-form-field appearance="outline"><mat-label>CEP (8 dígitos)</mat-label><input matInput formControlName="zipCode" required maxlength="8" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Logradouro</mat-label><input matInput formControlName="street" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Número</mat-label><input matInput formControlName="streetNumber" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Complemento</mat-label><input matInput formControlName="complement" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Bairro</mat-label><input matInput formControlName="neighborhood" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Cidade</mat-label><input matInput formControlName="city" /></mat-form-field>
                <mat-form-field appearance="outline"><mat-label>Estado</mat-label>
                  <mat-select formControlName="stateUF">
                    @for (uf of states; track uf.value) { <mat-option [value]="uf.value">{{ uf.label }}</mat-option> }
                  </mat-select>
                </mat-form-field>
                <mat-form-field appearance="outline"><mat-label>País</mat-label><input matInput formControlName="country" /></mat-form-field>
                <button mat-icon-button type="button" color="warn" (click)="removeAddress(i)"><mat-icon>remove_circle</mat-icon></button>
              </div>
            }
            <button mat-stroked-button type="button" (click)="addAddress()"><mat-icon>add</mat-icon> Adicionar Endereço</button>
          </div>

          <div class="form-actions"><button mat-button type="button" routerLink="/suppliers">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">{{ isEdit ? 'Salvar' : 'Criar' }}</button></div>
        </form>
      </mat-card-content></mat-card>
    }
  `,
  styles: [`.form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; } h3 { margin: 24px 0 12px; color: #1a1a2e; } .form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; } .full-width { grid-column: 1/-1; } .inline-form { display: flex; gap: 12px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; } .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }`],
})
export class SupplierFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private supplierService = inject(SupplierService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  isEdit = false; private supplierId = '';
  loading = signal(false); saving = signal(false);
  states = Object.values(StateUF).map(v => ({ value: v, label: STATE_UF_LABELS[v] }));

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    cnpj: ['', [Validators.required, Validators.minLength(14)]],
    tradeName: ['', Validators.required],
    website: [''],
    minOrderValue: [0],
    notes: [''],
    contacts: this.fb.array([]),
    addresses: this.fb.array([]),
  });

  get contacts(): FormArray { return this.form.get('contacts') as FormArray; }
  get addresses(): FormArray { return this.form.get('addresses') as FormArray; }

  ngOnInit(): void {
    this.supplierId = this.route.snapshot.paramMap.get('id') || '';
    this.isEdit = !!this.supplierId;
    this.addContact();
    this.addAddress();
  }

  addContact(): void {
    this.contacts.push(this.fb.group({ contactName: ['', Validators.required], email: ['', [Validators.required, Validators.email]], phoneNumber: ['', Validators.required] }));
  }

  removeContact(index: number): void { this.contacts.removeAt(index); }

  addAddress(): void {
    this.addresses.push(this.fb.group({
      label: [''], street: [''], streetNumber: [''], complement: [''],
      neighborhood: [''], city: [''], zipCode: ['', Validators.required],
      stateUF: [StateUF.SP], country: ['Brasil'], isMain: [false],
    }));
  }

  removeAddress(index: number): void { this.addresses.removeAt(index); }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const data = this.form.value;
    const req: SupplierRequest = {
      name: data.name, cnpj: data.cnpj, tradeName: data.tradeName,
      contacts: data.contacts, website: data.website,
      minOrderValue: data.minOrderValue, notes: data.notes,
      addresses: data.addresses.map((a: any) => ({ ...a, isMain: false })),
    };
    if (this.isEdit) {
      this.supplierService.update(this.supplierId, { tradeName: data.tradeName, website: data.website, minOrderValue: data.minOrderValue, notes: data.notes })
        .subscribe({ next: () => this.onSuccess('Fornecedor atualizado!'), error: () => this.saving.set(false) });
    } else {
      this.supplierService.create(req).subscribe({ next: () => this.onSuccess('Fornecedor criado!'), error: () => this.saving.set(false) });
    }
  }

  private onSuccess(msg: string): void {
    this.snackBar.open(msg, 'OK', { duration: 3000 });
    this.router.navigate(['/suppliers']);
  }
}
