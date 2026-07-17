import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../../environments/environment';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  let headers = req.headers;

  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  const apiReq = req.clone({
    url: req.url.startsWith('http') ? req.url : `${environment.apiUrl}${req.url}`,
    headers,
  });

  return next(apiReq);
};
