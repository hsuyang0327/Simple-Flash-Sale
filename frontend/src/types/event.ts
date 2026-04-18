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
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
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
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}
