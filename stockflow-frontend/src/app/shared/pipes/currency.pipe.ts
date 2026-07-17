import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'currencyBr', standalone: true })
export class CurrencyBrPipe implements PipeTransform {
  transform(value: number | undefined | null): string {
    if (value == null || isNaN(value)) return 'R$ 0,00';
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }
}
