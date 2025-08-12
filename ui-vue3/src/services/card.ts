import http from './http';

export type CardStatus = 'on' | 'off';

export interface Card {
  id: number;
  name: string;
  code: string;
  faceValue: number;
  price: number;
  stock: number;
  status: CardStatus;
  validFrom: string; // YYYY-MM-DD
  validTo: string;   // YYYY-MM-DD
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ListQuery {
  page: number;
  pageSize: number;
  name?: string;
  code?: string;
  status?: CardStatus | '';
  dateFrom?: string;
  dateTo?: string;
}

export interface ListResult {
  list: Card[];
  total: number;
}

export function getCards(params: ListQuery) {
  return http.get<ListResult>('/cards', { params });
}

export function createCard(data: Partial<Card>) {
  return http.post<Card>('/cards', data);
}

export function updateCard(id: number, data: Partial<Card>) {
  return http.put<Card>(`/cards/${id}`, data);
}

export function deleteCard(id: number) {
  return http.delete(`/cards/${id}`);
}

export function updateCardStatus(id: number, status: CardStatus) {
  return http.patch<Card>(`/cards/${id}/status`, { status });
} 