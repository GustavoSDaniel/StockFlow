import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import {
  SupplierResponse, SupplierSummaryResponse, SupplierRequest, SupplierUpdateRequest,
  AddressRequest, AddressResponse, SupplierContactRequest, SupplierContactResponse,
} from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  private api = `${environment.apiUrl}/api/v1/suppliers`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10, sort = 'name,asc'): Observable<Page<SupplierSummaryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<SupplierSummaryResponse>>(this.api, { params });
  }

  getByCnpj(cnpj: string): Observable<SupplierResponse> {
    return this.http.get<SupplierResponse>(`${this.api}/cnpj`, { params: { cnpj } });
  }

  searchByName(name: string, page = 0, size = 10): Observable<Page<SupplierSummaryResponse>> {
    const params = new HttpParams().set('name', name).set('page', page).set('size', size);
    return this.http.get<Page<SupplierSummaryResponse>>(`${this.api}/name`, { params });
  }

  searchByTradeName(tradeName: string, page = 0, size = 10): Observable<Page<SupplierSummaryResponse>> {
    const params = new HttpParams().set('tradeName', tradeName).set('page', page).set('size', size);
    return this.http.get<Page<SupplierSummaryResponse>>(`${this.api}/tradeName`, { params });
  }

  create(request: SupplierRequest): Observable<SupplierResponse> {
    return this.http.post<SupplierResponse>(this.api, request);
  }

  update(id: string, request: SupplierUpdateRequest): Observable<SupplierResponse> {
    return this.http.put<SupplierResponse>(`${this.api}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  addAddress(supplierId: string, request: AddressRequest): Observable<AddressResponse> {
    return this.http.post<AddressResponse>(`${this.api}/${supplierId}/address`, request);
  }

  deleteAddress(addressId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/address/${addressId}`);
  }

  addContact(supplierId: string, request: SupplierContactRequest): Observable<SupplierContactResponse> {
    return this.http.post<SupplierContactResponse>(`${this.api}/${supplierId}/contact`, request);
  }

  deleteContact(contactId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/contact/${contactId}`);
  }
}
