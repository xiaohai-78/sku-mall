import http from './http';

export interface OrderItem {
  id: number;
  orderId: string;
  dealTime: string; // YYYY-MM-DD HH:mm
  cardName: string;
  cardCode: string;
}

export interface OrderListQuery {
  page: number;
  pageSize: number;
  orderId?: string;
  cardName?: string;
  cardCode?: string;
  dateFrom?: string; // YYYY-MM-DD
  dateTo?: string;   // YYYY-MM-DD
}

export interface OrderListResult {
  list: OrderItem[];
  total: number;
}

export function getOrders(params: OrderListQuery) {
  return http.get<OrderListResult>('/orders', { params });
} 