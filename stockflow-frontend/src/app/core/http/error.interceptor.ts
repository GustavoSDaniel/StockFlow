import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth/auth.service';
import { ProblemDetail } from '../models/problem-detail.model';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const auth = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const problem = error.error as ProblemDetail;

      switch (error.status) {
        case 401:
          snackBar.open('Sessão expirada. Faça login novamente.', 'OK', { duration: 5000 });
          auth.login();
          break;
        case 403:
          snackBar.open('Acesso negado. Você não tem permissão para esta ação.', 'OK', { duration: 5000 });
          break;
        case 409:
          snackBar.open(problem?.detail || 'Conflito de concorrência. Recarregue e tente novamente.', 'OK', { duration: 5000 });
          break;
        case 400:
          if (problem?.fieldsErrors) {
            // erros de validação - tratados pelo formulário
          } else {
            snackBar.open(problem?.detail || 'Requisição inválida.', 'OK', { duration: 4000 });
          }
          break;
        case 404:
          snackBar.open(problem?.detail || 'Recurso não encontrado.', 'OK', { duration: 4000 });
          break;
        case 422:
          snackBar.open(problem?.detail || 'Violação de regra de negócio.', 'OK', { duration: 5000 });
          break;
        case 500:
          snackBar.open('Erro interno no servidor. Tente novamente mais tarde.', 'OK', { duration: 5000 });
          break;
        case 503:
          snackBar.open('Serviço temporariamente indisponível.', 'OK', { duration: 5000 });
          break;
        default:
          snackBar.open(problem?.detail || 'Ocorreu um erro inesperado.', 'OK', { duration: 4000 });
      }

      return throwError(() => error);
    })
  );
};
