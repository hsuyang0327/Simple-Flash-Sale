export interface OrderAdminResponse {
  orderId: string;
  memberId: string;
  memberName: string;
  productId: string;
  productName: string;
  quantity: number;
  totalPrice: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderAdminPageResponse {
  content: OrderAdminResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface OrderSearchParams {
  productName?: string;
  memberName?: string;
  page?: number;
  size?: number;
}

export interface OrderClientDetailResponse {
  orderId: string;
  productId: string;
  productName: string;
  quantity: number;
  totalPrice: number;
  status: string;
  createdAt: string;
}

export interface OrderClientPageResponse {
  content: OrderClientDetailResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface OrderStatusResponse {
  status: 'SUCCESS' | 'PENDING';
  order?: OrderClientDetailResponse;
}