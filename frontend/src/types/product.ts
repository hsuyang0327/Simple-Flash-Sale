export interface ProductAdminResponse {
  productId: string;
  productName: string;
  description?: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProductClientResponse {
  productId: string;
  productName: string;
  description?: string;
}

export interface ProductAdminPageResponse {
  content: ProductAdminResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ProductSearchParams {
  productName?: string;
  page?: number;
  size?: number;
}

export interface ProductRequest {
  productName: string;
  description?: string;
  status: number;
}
