import http from './http';

export interface SoldItemResponse {
  cardName: string;
  cardCode: string;
  skuKey: string;
  orderId: string;
  dealTime: string;
}

export function sellCard(cardCode: string) {
  return http.post<SoldItemResponse>('/sales', { cardCode });
} 