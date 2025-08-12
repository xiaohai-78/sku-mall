import type { MockMethod } from 'vite-plugin-mock';

interface OrderItem {
  id: number;
  orderId: string;
  dealTime: string; // YYYY-MM-DD HH:mm:ss
  cardName: string;
  cardCode: string;
}

function pad2(n: number) { return n < 10 ? `0${n}` : String(n); }

// 辅助函数：将 Date 对象格式化为中国时区 (UTC+8) 的 YYYY-MM-DD HH:mm:ss 字符串
function formatDateTimeToChineseTimezone(date: Date) {
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
    timeZone: 'Asia/Shanghai',
  };
  // Intl.DateTimeFormat 返回的日期格式可能因 locale 而异，这里手动调整为 YYYY-MM-DD HH:mm:ss
  const parts = new Intl.DateTimeFormat('zh-CN', options).formatToParts(date);
  const year = parts.find(p => p.type === 'year')!.value;
  const month = parts.find(p => p.type === 'month')!.value;
  const day = parts.find(p => p.type === 'day')!.value;
  const hour = parts.find(p => p.type === 'hour')!.value;
  const minute = parts.find(p => p.type === 'minute')!.value;
  const second = parts.find(p => p.type === 'second')!.value;
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

const now = new Date();
const baseYear = now.getFullYear();

const names = ['点卡-A', '点卡-B', '点卡-C', '点卡-D', '点卡-E'];

const orders: OrderItem[] = Array.from({ length: 57 }).map((_, i) => {
  const id = i + 1;
  const cardName = names[i % names.length];
  const cardCode = `CARD-${1100 + i}`;
  
  const orderDate = new Date();
  orderDate.setFullYear(baseYear, ((i % 12) + 1) - 1, ((i % 27) + 1)); // Month is 0-indexed
  orderDate.setHours((i * 3) % 24, (i * 7) % 60, (i * 11) % 60);

  return {
    id,
    orderId: `ORD-${Date.now()}-${1000 + i}`,
    dealTime: formatDateTimeToChineseTimezone(orderDate),
    cardName,
    cardCode
  };
});

const ok = (data: any) => ({ code: 0, data, message: 'ok' });

export default [
  {
    url: '/api/orders',
    method: 'get',
    response: (opts: any) => {
      const query = opts.query || {};
      const page = Number(query.page || 1);
      const pageSize = Number(query.pageSize || 10);
      const orderId = (query.orderId || '').toString().toLowerCase();
      const cardName = (query.cardName || '').toString().toLowerCase();
      const cardCode = (query.cardCode || '').toString().toLowerCase();
      const dateFrom = query.dateFrom ? `${query.dateFrom} 00:00:00` : '';
      const dateTo = query.dateTo ? `${query.dateTo} 23:59:59` : '';

      let list = orders.slice();

      // default sort by latest dealTime desc
      list.sort((a, b) => new Date(b.dealTime).getTime() - new Date(a.dealTime).getTime());

      if (orderId) list = list.filter((o) => o.orderId.toLowerCase().includes(orderId));
      if (cardName) list = list.filter((o) => o.cardName.toLowerCase().includes(cardName));
      if (cardCode) list = list.filter((o) => o.cardCode.toLowerCase().includes(cardCode));
      // 确保日期过滤是基于完整时间戳字符串进行的比较
      if (dateFrom) list = list.filter((o) => o.dealTime >= dateFrom);
      if (dateTo) list = list.filter((o) => o.dealTime <= dateTo);

      const total = list.length;
      const start = (page - 1) * pageSize;
      const end = start + pageSize;
      return ok({ list: list.slice(start, end), total });
    }
  }
] as MockMethod[]; 