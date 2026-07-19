import {
  ProductStatus, StockStatus, MovementType, MovementReason,
  UnitMeasure, StateUF, UserRole, NotificationType, NotificationPriority,
} from './enums';

// ─── Produto ───

export interface ProductResponse {
  id: string;
  name: string;
  description: string;
  sku: string;
  barcode: string;
  categoryId: string;
  categoryName: string;
  supplierId: string;
  supplierName: string;
  costPrice: number;
  salePrice: number;
  unitMeasure: UnitMeasure;
  status: ProductStatus;
  margin: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProductRequest {
  name: string;
  description: string;
  categoryId: string;
  supplierId: string;
  costPrice: number;
  salePrice: number;
  unitMeasure: UnitMeasure;
  barcode: string;
}

export interface ProductUpdateRequest {
  name: string;
  description: string;
  costPrice: number;
  salePrice: number;
  unitMeasure: UnitMeasure;
  barcode: string;
}

// ─── Estoque ───

export interface StockResponse {
  id: string;
  productId: string;
  productName: string;
  productSku: string;
  currentQuantity: number;
  minimumQuantity: number;
  maximumQuantity: number;
  reorderPoint: number;
  reorderQuantity: number;
  stockStatus: StockStatus;
  location: string;
  warehouseId: string;
  createdAt: string;
  updatedAt: string;
}

export interface StockSummaryResponse {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  currentQuantity: number;
  status: StockStatus;
  warehouseId: string;
  location: string;
}

export interface StockRequest {
  minimumQuantity: number;
  maximumQuantity: number;
  reorderPoint: number;
  reorderQuantity: number;
  location: string;
  warehouseId: string;
}

export interface StockUpdate {
  minimumQuantity: number;
  maximumQuantity: number;
  reorderPoint: number;
  reorderQuantity: number;
  location: string;
  warehouseId: string;
}

export interface InventoryMovementRequest {
  movementType: MovementType;
  quantity: number;
  movementReason: MovementReason;
  referenceNumber: string;
  supplierId: string;
  customerId: string;
  note: string;
  unitCost: number;
}

export interface TransferRequest {
  quantity: number;
  sourceWarehouseId: string;
  targetWarehouseId: string;
  referenceNumber: string;
  note: string;
}

export interface InventoryMovementResponse {
  id: string;
  productId: string;
  productName: string;
  stockId: string;
  movementType: MovementType;
  quantity: number;
  quantityBefore: number;
  quantityAfter: number;
  movementReason: MovementReason;
  referenceNumber: string;
  supplierId: string;
  supplierName: string;
  customerId: string;
  note: string;
  unitCost: number;
  createdAt: string;
}

// ─── Categoria ───

export interface CategoryResponse {
  id: string;
  name: string;
  description: string;
  parentId: string;
  parentName: string;
  active: boolean;
  subCategories: CategoryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  name: string;
  description: string;
  parentId: string;
}

export interface CategoryUpdateRequest {
  name: string;
  description: string;
}

// ─── Fornecedor ───

export interface SupplierResponse {
  id: string;
  name: string;
  cnpj: string;
  tradeName: string;
  website: string;
  minOrderValue: number;
  notes: string;
  contacts: SupplierContactResponse[];
  addresses: AddressResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface SupplierSummaryResponse {
  id: string;
  name: string;
  cnpj: string;
  tradeName: string;
}

export interface SupplierRequest {
  name: string;
  cnpj: string;
  tradeName: string;
  contacts: SupplierContactRequest[];
  website: string;
  minOrderValue: number;
  notes: string;
  addresses: AddressRequest[];
}

export interface SupplierUpdateRequest {
  tradeName: string;
  website: string;
  minOrderValue: number;
  notes: string;
}

export interface SupplierContactRequest {
  contactName: string;
  email: string;
  phoneNumber: string;
}

export interface SupplierContactResponse {
  id: string;
  contactName: string;
  email: string;
  phoneNumber: string;
  active: boolean;
}

export interface AddressRequest {
  label: string;
  street: string;
  streetNumber: string;
  complement: string;
  neighborhood: string;
  city: string;
  zipCode: string;
  stateUF: StateUF;
  country: string;
  isMain: boolean;
  /** true = endereço preenchido manualmente (ignorar ViaCEP); false = CEP válido consultado */
  hasManualAddress: boolean;
}

export interface AddressResponse {
  id: string;
  label: string;
  street: string;
  streetNumber: string;
  complement: string;
  neighborhood: string;
  city: string;
  zipCode: string;
  stateUF: StateUF;
  country: string;
  isMain: boolean;
  active: boolean;
}

// ─── Usuário ───

export interface UserResponse {
  id: string;
  userName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

// ─── Notificação ───

export interface NotificationResponse {
  id: string;
  productId: string;
  productName: string;
  productSku: string;
  notificationType: NotificationType;
  notificationPriority: NotificationPriority;
  title: string;
  message: string;
  currentQuantity: number;
  minimumQuantity: number;
  maximumQuantity: number;
  reorderPoint: number;
  read: boolean;
  resolved: boolean;
  readAt: string;
  resolvedAt: string;
  createdAt: string;
}

export interface NotificationFilter {
  from?: string;
  to?: string;
  type?: NotificationType;
  priority?: NotificationPriority;
  read?: boolean;
  resolved?: boolean;
}

// ─── Dashboard ───

export interface DashboardOverviewResponse {
  products: ProductStats;
  financials: FinancialStats;
  totalSuppliers: number;
  totalCategories: number;
  totalNotifications: number;
}

export interface ProductStats {
  total: number;
  active: number;
  inactive: number;
  discontinued: number;
}

export interface FinancialStats {
  totalStockValue: number;
  potentialSalesValue: number;
  averageMarginPercentage: number;
}

export interface ProductStockItem {
  productId: string;
  productName: string;
  sku: string;
  currentQuantity: number;
  minimumQuantity: number;
  status: StockStatus;
}

export interface DashboardStockResponse {
  statusCounts: StockStatusCounts;
  top10LowestStock: ProductStockItem[];
  top10HighestStock: ProductStockItem[];
}

export interface StockStatusCounts {
  outOfStock: number;
  lowStock: number;
  reorderPoint: number;
  normal: number;
  overStocked: number;
}

export interface MovementsByType {
  movementType: MovementType;
  total: number;
}

export interface MovementsByReason {
  movementReason: MovementReason;
  total: number;
}

export interface DailyHistory {
  date: string;
  totalMovements: number;
}

export interface TopMovedProduct {
  productId: string;
  productName: string;
  sku: string;
  totalQuantityMoved: number;
}

export interface DashboardMovementsResponse {
  summary: MovementSummary;
  movementsByType: MovementsByType[];
  movementsByReason: MovementsByReason[];
  dailyHistories: DailyHistory[];
  topMovedProducts: TopMovedProduct[];
}

export interface MovementSummary {
  totalMovementsToday: number;
  entriesThisMonth: number;
  exitsThisMonth: number;
}

export interface DashboardSupplierItem {
  id: string;
  name: string;
  totalProducts: number;
  totalStockValue: number;
}

export interface DashboardSupplierResponse {
  suppliers: DashboardSupplierItem[];
}

// ─── Error Doc ───

export interface ErrorDocResponse {
  errorKey: string;
  description: string;
  status: number;
  problemType: string;
}
