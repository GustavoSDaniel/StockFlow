// ─── Roles de Usuário ───
export enum UserRole {
  EMPLOYEE = 'EMPLOYEE',
  MANAGER = 'MANAGER',
  ADMIN = 'ADMIN',
}

// ─── Status do Produto ───
export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED',
}

// ─── Status do Estoque (computado) ───
export enum StockStatus {
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  LOW = 'LOW',
  REORDER_POINT = 'REORDER_POINT',
  NORMAL = 'NORMAL',
  OVER_STOCKED = 'OVER_STOCKED',
}

// ─── Tipos de Movimentação ───
export enum MovementType {
  ENTRY = 'ENTRY',
  EXIT = 'EXIT',
  TRANSFER = 'TRANSFER',
  RETURN = 'RETURN',
  ADJUSTMENT = 'ADJUSTMENT',
}

// ─── Motivos de Movimentação ───
export enum MovementReason {
  PURCHASE = 'PURCHASE',
  RETURN_CUSTOMER = 'RETURN_CUSTOMER',
  WARRANTY_REPLACEMENT = 'WARRANTY_REPLACEMENT',
  SALE = 'SALE',
  PROMOTIONAL_GIFT = 'PROMOTIONAL_GIFT',
  INTERNAL_USE = 'INTERNAL_USE',
  QUALITY_CHECK = 'QUALITY_CHECK',
  RETURN_SUPPLIER = 'RETURN_SUPPLIER',
  INVENTORY_COUNT = 'INVENTORY_COUNT',
  LOSS = 'LOSS',
  THEFT = 'THEFT',
  DAMAGE = 'DAMAGE',
  EXPIRATION = 'EXPIRATION',
  TRANSFER = 'TRANSFER',
}

// ─── Unidades de Medida ───
export enum UnitMeasure {
  UN = 'UN',
  KIT = 'KIT',
  KG = 'KG',
  G = 'G',
  L = 'L',
  ML = 'ML',
  M = 'M',
  CX = 'CX',
  PR = 'PR',
  PC = 'PC',
}

// ─── Estados Brasileiros ───
export enum StateUF {
  AC = 'AC', AL = 'AL', AP = 'AP', AM = 'AM', BA = 'BA', CE = 'CE',
  DF = 'DF', ES = 'ES', GO = 'GO', MA = 'MA', MT = 'MT', MS = 'MS',
  MG = 'MG', PA = 'PA', PB = 'PB', PR = 'PR', PE = 'PE', PI = 'PI',
  RJ = 'RJ', RN = 'RN', RS = 'RS', RO = 'RO', RR = 'RR', SC = 'SC',
  SP = 'SP', SE = 'SE', TO = 'TO', EX = 'EX',
}

// ─── Tipos de Notificação ───
export enum NotificationType {
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  STOCK_LOW = 'STOCK_LOW',
  REORDER_POINT = 'REORDER_POINT',
  OVERSTOCK = 'OVERSTOCK',
}

// ─── Prioridade de Notificação ───
export enum NotificationPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

// ─── Labels em Português ───

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  [UserRole.EMPLOYEE]: 'Funcionário',
  [UserRole.MANAGER]: 'Gerente',
  [UserRole.ADMIN]: 'Administrador',
};

export const PRODUCT_STATUS_LABELS: Record<ProductStatus, string> = {
  [ProductStatus.ACTIVE]: 'Ativo',
  [ProductStatus.INACTIVE]: 'Inativo',
  [ProductStatus.DISCONTINUED]: 'Descontinuado',
};

export const STOCK_STATUS_LABELS: Record<StockStatus, string> = {
  [StockStatus.OUT_OF_STOCK]: 'Sem Estoque',
  [StockStatus.LOW]: 'Estoque Baixo',
  [StockStatus.REORDER_POINT]: 'Ponto de Reposição',
  [StockStatus.NORMAL]: 'Normal',
  [StockStatus.OVER_STOCKED]: 'Excesso',
};

export const MOVEMENT_TYPE_LABELS: Record<MovementType, string> = {
  [MovementType.ENTRY]: 'Entrada',
  [MovementType.EXIT]: 'Saída',
  [MovementType.TRANSFER]: 'Transferência',
  [MovementType.RETURN]: 'Devolução',
  [MovementType.ADJUSTMENT]: 'Ajuste',
};

export const MOVEMENT_REASON_LABELS: Record<MovementReason, string> = {
  [MovementReason.PURCHASE]: 'Compra',
  [MovementReason.RETURN_CUSTOMER]: 'Devolução de Cliente',
  [MovementReason.WARRANTY_REPLACEMENT]: 'Troca de Garantia',
  [MovementReason.SALE]: 'Venda',
  [MovementReason.PROMOTIONAL_GIFT]: 'Brinde Promocional',
  [MovementReason.INTERNAL_USE]: 'Uso Interno',
  [MovementReason.QUALITY_CHECK]: 'Controle de Qualidade',
  [MovementReason.RETURN_SUPPLIER]: 'Devolução ao Fornecedor',
  [MovementReason.INVENTORY_COUNT]: 'Contagem de Inventário',
  [MovementReason.LOSS]: 'Perda',
  [MovementReason.THEFT]: 'Furto',
  [MovementReason.DAMAGE]: 'Dano',
  [MovementReason.EXPIRATION]: 'Vencimento',
  [MovementReason.TRANSFER]: 'Transferência',
};

export const UNIT_MEASURE_LABELS: Record<UnitMeasure, string> = {
  [UnitMeasure.UN]: 'Unidade',
  [UnitMeasure.KIT]: 'Kit',
  [UnitMeasure.KG]: 'Quilograma',
  [UnitMeasure.G]: 'Grama',
  [UnitMeasure.L]: 'Litro',
  [UnitMeasure.ML]: 'Mililitro',
  [UnitMeasure.M]: 'Metro',
  [UnitMeasure.CX]: 'Caixa',
  [UnitMeasure.PR]: 'Par',
  [UnitMeasure.PC]: 'Peça',
};

export const STATE_UF_LABELS: Record<StateUF, string> = {
  [StateUF.AC]: 'Acre', [StateUF.AL]: 'Alagoas', [StateUF.AP]: 'Amapá',
  [StateUF.AM]: 'Amazonas', [StateUF.BA]: 'Bahia', [StateUF.CE]: 'Ceará',
  [StateUF.DF]: 'Distrito Federal', [StateUF.ES]: 'Espírito Santo',
  [StateUF.GO]: 'Goiás', [StateUF.MA]: 'Maranhão', [StateUF.MT]: 'Mato Grosso',
  [StateUF.MS]: 'Mato Grosso do Sul', [StateUF.MG]: 'Minas Gerais',
  [StateUF.PA]: 'Pará', [StateUF.PB]: 'Paraíba', [StateUF.PR]: 'Paraná',
  [StateUF.PE]: 'Pernambuco', [StateUF.PI]: 'Piauí',
  [StateUF.RJ]: 'Rio de Janeiro', [StateUF.RN]: 'Rio Grande do Norte',
  [StateUF.RS]: 'Rio Grande do Sul', [StateUF.RO]: 'Rondônia',
  [StateUF.RR]: 'Roraima', [StateUF.SC]: 'Santa Catarina',
  [StateUF.SP]: 'São Paulo', [StateUF.SE]: 'Sergipe', [StateUF.TO]: 'Tocantins',
  [StateUF.EX]: 'Exterior',
};

export const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  [NotificationType.OUT_OF_STOCK]: 'Sem Estoque',
  [NotificationType.STOCK_LOW]: 'Estoque Baixo',
  [NotificationType.REORDER_POINT]: 'Ponto de Reposição',
  [NotificationType.OVERSTOCK]: 'Excesso de Estoque',
};

export const NOTIFICATION_PRIORITY_LABELS: Record<NotificationPriority, string> = {
  [NotificationPriority.LOW]: 'Baixa',
  [NotificationPriority.MEDIUM]: 'Média',
  [NotificationPriority.HIGH]: 'Alta',
  [NotificationPriority.CRITICAL]: 'Crítica',
};

// ─── Razões válidas por tipo de movimentação ───
export const VALID_REASONS_BY_TYPE: Record<MovementType, MovementReason[]> = {
  [MovementType.ENTRY]: [
    MovementReason.PURCHASE,
    MovementReason.RETURN_CUSTOMER,
    MovementReason.WARRANTY_REPLACEMENT,
    MovementReason.TRANSFER,
  ],
  [MovementType.EXIT]: [
    MovementReason.SALE,
    MovementReason.PROMOTIONAL_GIFT,
    MovementReason.INTERNAL_USE,
    MovementReason.LOSS,
    MovementReason.THEFT,
    MovementReason.DAMAGE,
    MovementReason.EXPIRATION,
    MovementReason.TRANSFER,
  ],
  [MovementType.TRANSFER]: [MovementReason.TRANSFER],
  [MovementType.RETURN]: [
    MovementReason.RETURN_CUSTOMER,
    MovementReason.RETURN_SUPPLIER,
    MovementReason.WARRANTY_REPLACEMENT,
  ],
  [MovementType.ADJUSTMENT]: [
    MovementReason.INVENTORY_COUNT,
    MovementReason.QUALITY_CHECK,
    MovementReason.LOSS,
    MovementReason.DAMAGE,
  ],
};
