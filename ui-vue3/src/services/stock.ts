import http from './http';

interface AddStockResponse {
  message: string;
}

export function addStock(cardCode: string, skuKey: string) {
  return http.post<AddStockResponse>('/stock', { cardCode, skuKey });
} 