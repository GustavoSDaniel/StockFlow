import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import { ProductResponse, ProductRequest, ProductUpdateRequest } from '../models/domain.models';
import { ProductStatus } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private api = `${environment.apiUrl}/api/v1/products`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10, sort = 'name,asc'): Observable<Page<ProductResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<ProductResponse>>(this.api, { params });
  }

  getById(id: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.api}/${id}`);
  }

  getBySku(sku: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.api}/sku`, { params: { sku } });
  }

  getByStatus(status: ProductStatus, page = 0, size = 10): Observable<Page<ProductResponse>> {
    const params = new HttpParams().set('status', status).set('page', page).set('size', size);
    return this.http.get<Page<ProductResponse>>(`${this.api}/status`, { params });
  }

  getByCategory(categoryId: string, page = 0, size = 10): Observable<Page<ProductResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ProductResponse>>(`${this.api}/category/${categoryId}`, { params });
  }

  getBySupplier(supplierId: string, page = 0, size = 10): Observable<Page<ProductResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ProductResponse>>(`${this.api}/supplier/${supplierId}`, { params });
  }

  searchByName(name: string, page = 0, size = 10): Observable<Page<ProductResponse>> {
    const params = new HttpParams().set('name', name).set('page', page).set('size', size);
    return this.http.get<Page<ProductResponse>>(`${this.api}/search-name`, { params });
  }

  searchByNameAndStatus(name: string, status: ProductStatus, page = 0, size = 10): Observable<Page<ProductResponse>> {
    const params = new HttpParams().set('name', name).set('status', status).set('page', page).set('size', size);
    return this.http.get<Page<ProductResponse>>(`${this.api}/search-name-status`, { params });
  }

  create(request: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.api, request);
  }

  update(id: string, request: ProductUpdateRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.api}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  activate(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/activate`, {});
  }

  inactive(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/inactive`, {});
  }

  discontinue(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/discontinue`, {});
  }

  downloadPdfReport(status?: string): Observable<Blob> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get(`${this.api}/report/pdf`, { params, responseType: 'blob' });
  }
}
