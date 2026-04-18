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
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
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
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface OrderStatusResponse {
  status: 'SUCCESS' | 'PENDING';
  order?: OrderClientDetailResponse;
}