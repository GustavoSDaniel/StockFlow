import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import {
  StockResponse, StockSummaryResponse, StockRequest, StockUpdate,
  InventoryMovementRequest, InventoryMovementResponse, TransferRequest,
} from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class StockService {
  private api = `${environment.apiUrl}/api/v1/stocks`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10): Observable<Page<StockSummaryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<StockSummaryResponse>>(this.api, { params });
  }

  getById(id: string): Observable<StockResponse> {
    return this.http.get<StockResponse>(`${this.api}/${id}`);
  }

  getByProduct(productId: string, page = 0, size = 10): Observable<Page<StockResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<StockResponse>>(`${this.api}/product/${productId}`, { params });
  }

  getMovements(stockId: string, page = 0, size = 10): Observable<Page<InventoryMovementResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<InventoryMovementResponse>>(`${this.api}/${stockId}/movements`, { params });
  }

  create(productId: string, request: StockRequest): Observable<StockResponse> {
    return this.http.post<StockResponse>(`${this.api}/${productId}`, request);
  }

  update(id: string, request: StockUpdate): Observable<StockResponse> {
    return this.http.put<StockResponse>(`${this.api}/${id}`, request);
  }

  entry(id: string, request: InventoryMovementRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/${id}/entry`, request);
  }

  exit(id: string, request: InventoryMovementRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/${id}/exit`, request);
  }

  adjust(id: string, request: InventoryMovementRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/${id}/adjust`, request);
  }

  transfer(productId: string, request: TransferRequest): Observable<void> {
    return this.http.post<void>(`${this.api}/${productId}/transfer`, request);
  }

  getOutOfStock(page = 0, size = 10): Observable<Page<StockSummaryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<StockSummaryResponse>>(`${this.api}/out-of-stock`, { params });
  }

  getLowStock(page = 0, size = 10): Observable<Page<StockSummaryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<StockSummaryResponse>>(`${this.api}/low-stock`, { params });
  }

  getOverStock(page = 0, size = 10): Observable<Page<StockSummaryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<StockSummaryResponse>>(`${this.api}/over-stock`, { params });
  }
}
