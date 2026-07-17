import { Pipe, PipeTransform } from '@angular/core';
import {
  USER_ROLE_LABELS, PRODUCT_STATUS_LABELS, STOCK_STATUS_LABELS,
  MOVEMENT_TYPE_LABELS, MOVEMENT_REASON_LABELS, UNIT_MEASURE_LABELS,
  STATE_UF_LABELS, NOTIFICATION_TYPE_LABELS, NOTIFICATION_PRIORITY_LABELS,
} from '../../core/models/enums';

type EnumLabelMap = Record<string, string>;

@Pipe({ name: 'enumLabel', standalone: true })
export class EnumLabelPipe implements PipeTransform {
  private static readonly maps: Record<string, EnumLabelMap> = {
    userRole: USER_ROLE_LABELS,
    productStatus: PRODUCT_STATUS_LABELS,
    stockStatus: STOCK_STATUS_LABELS,
    movementType: MOVEMENT_TYPE_LABELS,
    movementReason: MOVEMENT_REASON_LABELS,
    unitMeasure: UNIT_MEASURE_LABELS,
    stateUF: STATE_UF_LABELS,
    notificationType: NOTIFICATION_TYPE_LABELS,
    notificationPriority: NOTIFICATION_PRIORITY_LABELS,
  };

  transform(value: string | undefined | null, mapName: string): string {
    if (!value) return '-';
    const map = EnumLabelPipe.maps[mapName];
    return map?.[value] ?? value;
  }
}
