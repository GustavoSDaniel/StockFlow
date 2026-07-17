/** Resposta paginada da API Spring (Page<T>) */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/** Parâmetros de paginação e ordenação */
export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

/** Converte PageParams para query params de URL */
export function toPageQueryParams(params: PageParams): Record<string, string | number> {
  const query: Record<string, string | number> = {};
  if (params.page !== undefined) query['page'] = params.page;
  if (params.size !== undefined) query['size'] = params.size;
  if (params.sort) query['sort'] = params.sort;
  return query;
}

/** Página vazia - fallback */
export function emptyPage<T>(): Page<T> {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
    first: true,
    last: true,
    empty: true,
  };
}
