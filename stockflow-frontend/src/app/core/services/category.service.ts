import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import { CategoryResponse, CategoryRequest, CategoryUpdateRequest } from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private api = `${environment.apiUrl}/api/v1/categories`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 20, sort = 'name,asc'): Observable<Page<CategoryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<CategoryResponse>>(this.api, { params });
  }

  search(name: string, page = 0, size = 20): Observable<Page<CategoryResponse>> {
    const params = new HttpParams().set('name', name).set('page', page).set('size', size);
    return this.http.get<Page<CategoryResponse>>(`${this.api}/search`, { params });
  }

  searchActive(name: string, page = 0, size = 20): Observable<Page<CategoryResponse>> {
    const params = new HttpParams().set('name', name).set('page', page).set('size', size);
    return this.http.get<Page<CategoryResponse>>(`${this.api}/search-active`, { params });
  }

  getAllSubcategories(parentId: string, page = 0, size = 20): Observable<Page<CategoryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<CategoryResponse>>(`${this.api}/${parentId}/all-subcategories`, { params });
  }

  getAllActiveSubcategories(parentId: string, page = 0, size = 20): Observable<Page<CategoryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<CategoryResponse>>(`${this.api}/${parentId}/all-active-subcategories`, { params });
  }

  getAllDisabledCategories(page = 0, size = 20): Observable<Page<CategoryResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<CategoryResponse>>(`${this.api}/all-disable-categories`, { params });
  }

  create(request: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(this.api, request);
  }

  update(id: string, request: CategoryUpdateRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.api}/${id}`, request);
  }

  addSubcategory(parentId: string, childId: string): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.api}/${parentId}/subcategory/${childId}`, {});
  }

  removeSubcategory(parentId: string, childId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${parentId}/remove-subcategory/${childId}`);
  }

  activate(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/activate`, {});
  }

  disable(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/disable`, {});
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
