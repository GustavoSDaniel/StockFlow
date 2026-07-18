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
  totalProducts: number;
  activeProducts: number;
  inactiveProducts: number;
  discontinuedProducts: number;
  totalStockValue: number;
  potentialSalesValue: number;
  averageMargin: number;
  totalSuppliers: number;
  totalCategories: number;
  pendingCriticalNotifications: number;
  pendingHighNotifications: number;
}

export interface TopStockItem {
  productId: string;
  productName: string;
  sku: string;
  currentQuantity: number;
  status: StockStatus;
}

export interface DashboardStockResponse {
  outOfStockCount: number;
  lowStockCount: number;
  reorderPointCount: number;
  normalCount: number;
  overStockedCount: number;
  top10LowStock: TopStockItem[];
  top10HighStock: TopStockItem[];
}

export interface MovementsByType {
  movementType: MovementType;
  count: number;
  totalQuantity: number;
}

export interface MovementsByReason {
  movementReason: MovementReason;
  count: number;
  totalQuantity: number;
}

export interface DailyHistory {
  date: string;
  entries: number;
  exits: number;
  totalMovements: number;
}

export interface TopMovedProduct {
  productId: string;
  productName: string;
  sku: string;
  totalMoved: number;
}

export interface DashboardMovementsResponse {
  todayMovements: number;
  monthEntries: number;
  monthExits: number;
  movementsByType: MovementsByType[];
  movementsByReason: MovementsByReason[];
  dailyHistory: DailyHistory[];
  top10MovedProducts: TopMovedProduct[];
}

export interface DashboardSupplierItem {
  supplierId: string;
  supplierName: string;
  tradeName: string;
  productCount: number;
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
