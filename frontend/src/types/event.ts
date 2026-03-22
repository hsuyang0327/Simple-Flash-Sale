export interface EventResponse {
  eventId: string;
  price: number;
  stock: number;
  startTime: string;
  endTime: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface EventPageResponse {
  content: EventResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface EventSearchParams {
  productId: string;
  page?: number;
  size?: number;
}

export interface EventRequest {
  productId: string;
  price: number;
  stock: number;
  startTime: string;
  endTime: string;
  status: number;
}

// Client-facing preheated product from Redis
export interface EventProductDTO {
  productId: string;
  eventId: string;
  productName: string;
  description: string;
  price: string;
  stock: string;
  startTime: string;
  endTime: string;
}

export interface EventProductPageResponse {
  content: EventProductDTO[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
