/** Erro RFC 7807 Problem Details */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  timestamp?: string;
  fieldsErrors?: Record<string, string>;
}
