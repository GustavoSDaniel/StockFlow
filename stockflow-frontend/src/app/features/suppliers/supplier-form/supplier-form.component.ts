import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SupplierService } from '../../../core/services/supplier.service';
import { StateUF, STATE_UF_LABELS } from '../../../core/models/enums';
import { SupplierRequest, SupplierContactRequest, AddressRequest } from '../../../core/models/domain.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { debounceTime, distinctUntilChanged, of, switchMap, catchError } from 'rxjs';

interface ViaCepResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  unidade: string;
  bairro: string;
  localidade: string;
  uf: string;
  estado: string;
  erro?: boolean;
}

@Component({
  selector: 'app-supplier-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatCheckboxModule, MatSelectModule, MatButtonModule, MatCardModule, MatIconModule, LoadingSpinnerComponent],
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
              <div [formGroupName]="i" class="address-block">
                <div class="address-toggle">
                  <mat-checkbox formControlName="hasManualAddress">
                    Inserir endereço manualmente
                  </mat-checkbox>
                  @if (cepLoading[i]) {
                    <mat-icon class="cep-spinner">sync</mat-icon>
                  }
                </div>
                <div class="form-grid">
                  <mat-form-field appearance="outline" class="cep-field">
                    <mat-label>CEP (8 dígitos)</mat-label>
                    <input matInput formControlName="zipCode" maxlength="8" placeholder="00000000" />
                    @if (cepError[i]) {
                      <mat-hint class="cep-error">{{ cepError[i] }}</mat-hint>
                    }
                  </mat-form-field>
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
                </div>
                <button mat-icon-button type="button" color="warn" (click)="removeAddress(i)" class="remove-btn">
                  <mat-icon>remove_circle</mat-icon>
                </button>
              </div>
            }
            <button mat-stroked-button type="button" (click)="addAddress()"><mat-icon>add</mat-icon> Adicionar Endereço</button>
          </div>

          <div class="form-actions"><button mat-button type="button" routerLink="/suppliers">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">{{ isEdit ? 'Salvar' : 'Criar' }}</button></div>
        </form>
      </mat-card-content></mat-card>
    }
  `,
  styles: [`
    .form-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    h3 { margin: 24px 0 12px; color: #1a1a2e; }
    .form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; }
    .full-width { grid-column: 1/-1; }
    .inline-form { display: flex; gap: 12px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
    .address-block {
      border: 1px solid #e5e7eb; border-radius: 12px; padding: 16px; margin-bottom: 16px;
      position: relative;
    }
    .address-toggle {
      display: flex; align-items: center; gap: 8px; margin-bottom: 12px;
    }
    .cep-spinner { font-size: 18px; width: 18px; height: 18px; color: #7c3aed; animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .cep-field { position: relative; }
    .cep-error { color: #ef4444; font-size: 11px; }
    .remove-btn { position: absolute; top: 8px; right: 8px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
  `],
})
export class SupplierFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private supplierService = inject(SupplierService);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  isEdit = false; private supplierId = '';
  loading = signal(false); saving = signal(false);
  states = Object.values(StateUF).map(v => ({ value: v, label: STATE_UF_LABELS[v] }));

  /** Controla spinner de loading do CEP por índice do endereço */
  cepLoading: boolean[] = [];
  /** Mensagem de erro do CEP por índice do endereço */
  cepError: string[] = [];

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

  // ─── Contacts ───

  addContact(): void {
    this.contacts.push(this.fb.group({
      contactName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
    }));
  }

  removeContact(index: number): void { this.contacts.removeAt(index); }

  // ─── Addresses + ViaCEP ───

  addAddress(): void {
    const i = this.addresses.length;
    this.cepLoading[i] = false;
    this.cepError[i] = '';

    const group = this.fb.group({
      label: [''],
      street: [''],
      streetNumber: [''],
      complement: [''],
      neighborhood: [''],
      city: [''],
      zipCode: [''],
      stateUF: [StateUF.SP],
      country: ['Brasil'],
      isMain: [false],
      hasManualAddress: [false],
    });

    this.addresses.push(group);
    this.setupCepAutoFill(i);
  }

  removeAddress(index: number): void {
    this.addresses.removeAt(index);
    this.cepLoading.splice(index, 1);
    this.cepError.splice(index, 1);
    // Re-conecta listeners dos índices que deslocaram
    for (let i = index; i < this.addresses.length; i++) {
      this.setupCepAutoFill(i);
    }
  }

  /**
   * Monitora o campo CEP de um endereço. Ao digitar 8 dígitos,
   * consulta a API ViaCEP e preenche automaticamente logradouro,
   * bairro, cidade e estado — a menos que o checkbox "manual" esteja ativo.
   */
  private setupCepAutoFill(index: number): void {
    const addr = this.addresses.at(index) as FormGroup;
    const cepCtrl = addr.get('zipCode')!;
    const manualCtrl = addr.get('hasManualAddress')!;

    // Se o usuário marcar "manual", reseta o erro do CEP
    manualCtrl.valueChanges.pipe(distinctUntilChanged()).subscribe(manual => {
      if (manual) {
        this.cepError[index] = '';
      }
    });

    cepCtrl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
    ).subscribe(value => {
      const normalized = (value || '').replace(/\D/g, '');
      cepCtrl.setValue(normalized, { emitEvent: false });

      if (normalized.length !== 8) {
        this.cepLoading[index] = false;
        this.cepError[index] = '';
        return;
      }

      // Não consulta se modo manual está ativo
      if (manualCtrl.value) return;

      this.cepLoading[index] = true;
      this.cepError[index] = '';

      this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${normalized}/json/`)
        .pipe(
          catchError(() => of({ erro: true } as ViaCepResponse)),
        )
        .subscribe(res => {
          this.cepLoading[index] = false;

          if (res.erro) {
            this.cepError[index] = 'CEP não encontrado. Preencha manualmente.';
            return;
          }

          // Mapeia a UF do ViaCEP para o enum StateUF
          const uf = Object.values(StateUF).find(s => s === res.uf?.toUpperCase()) || StateUF.SP;

          addr.patchValue({
            street: res.logradouro || '',
            complement: res.complemento || '',
            neighborhood: res.bairro || '',
            city: res.localidade || '',
            stateUF: uf,
            hasManualAddress: false, // CEP válido → automático
          }, { emitEvent: false });
        });
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const data = this.form.value;
    const req: SupplierRequest = {
      name: data.name, cnpj: data.cnpj, tradeName: data.tradeName,
      contacts: data.contacts, website: data.website,
      minOrderValue: data.minOrderValue, notes: data.notes,
      addresses: data.addresses.map((a: any) => ({
        ...a,
        isMain: false,
        // CEP vazio + manual = flag explícita
        hasManualAddress: a.hasManualAddress || !a.zipCode || a.zipCode.length < 8,
      })),
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
