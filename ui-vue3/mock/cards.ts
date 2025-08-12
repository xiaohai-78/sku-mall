import type { MockMethod } from 'vite-plugin-mock';

interface CardSKU {
  key: string;
  sold: boolean;
}

interface Card {
  id: number;
  name: string;
  code: string;
  faceValue: number;
  price: number;
  _skus: CardSKU[]; // 内部维护的 SKU 列表
  status: 'on' | 'off';
  validFrom: string; // YYYY-MM-DD
  validTo: string;   // YYYY-MM-DD
  description?: string;
  createdAt: string;
  updatedAt: string;
}

let cardIdSeq = 1000;

function randInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pad2(n: number) { return n < 10 ? `0${n}` : String(n); }

function dateStr(y: number, m: number, d: number) { return `${y}-${pad2(m)}-${pad2(d)}`; }

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

const cards: Card[] = Array.from({ length: 35 }).map((_, i) => {
  const id = i + 1;
  const validFrom = dateStr(baseYear, randInt(1, 6), randInt(1, 20));
  const validTo = dateStr(baseYear + 1, randInt(7, 12), randInt(10, 28));
  
  const createdAtDate = new Date();
  createdAtDate.setHours(randInt(9, 17), randInt(0, 59), randInt(0, 59));
  const createdAt = formatDateTimeToChineseTimezone(createdAtDate);

  const updatedAtDate = new Date();
  updatedAtDate.setHours(randInt(9, 17), randInt(0, 59), randInt(0, 59));
  const updatedAt = formatDateTimeToChineseTimezone(updatedAtDate);

  const _skus: CardSKU[] = [];
  // 初始为每个点卡类型生成 5-10 个随机 SKU
  const initialSKUCount = randInt(5, 10);
  for (let j = 0; j < initialSKUCount; j++) {
    _skus.push({ key: `SKU-${id}-${j + 1}-${Date.now() + j}`, sold: false });
  }

  return {
    id,
    name: `点卡-${id}`,
    code: `CARD-${1000 + id}`,
    faceValue: randInt(10, 500),
    price: randInt(10, 500),
    _skus,
    status: Math.random() > 0.5 ? 'on' : 'off',
    validFrom,
    validTo,
    description: '示例点卡',
    createdAt,
    updatedAt
  };
});

const ok = (data: any) => ({ code: 0, data, message: 'ok' });
const fail = (message: string, code = 1) => ({ code, message });

export default [
  {
    url: '/api/cards',
    method: 'get',
    response: ({ query }) => {
      const page = Number(query.page || 1);
      const pageSize = Number(query.pageSize || 10);
      const name = (query.name || '').toString().toLowerCase();
      const code = (query.code || '').toString().toLowerCase();
      const status = query.status ? query.status.toString() : '';
      const dateFrom = query.dateFrom ? query.dateFrom.toString() : '';
      const dateTo = query.dateTo ? query.dateTo.toString() : '';

      let list = cards.slice();
      if (name) list = list.filter((c) => c.name.toLowerCase().includes(name));
      if (code) list = list.filter((c) => c.code.toLowerCase().includes(code));
      if (status) list = list.filter((c) => c.status === status);
      if (dateFrom) list = list.filter((c) => c.validFrom >= dateFrom);
      if (dateTo) list = list.filter((c) => c.validTo <= dateTo);

      const total = list.length;
      const start = (page - 1) * pageSize;
      const end = start + pageSize;

      // 在返回前计算当前可用的库存数量
      const pageList = list.slice(start, end).map(card => ({
        ...card,
        stock: card._skus.filter(s => !s.sold).length // 可用库存 = 未售出 SKU 数量
      }));

      return ok({ list: pageList, total });
    }
  },
  {
    url: '/api/cards',
    method: 'post',
    response: ({ body }) => {
      const now = new Date();
      const nowStr = formatDateTimeToChineseTimezone(now);
      const card: Card = {
        id: ++cardIdSeq,
        name: body.name,
        code: body.code,
        faceValue: Number(body.faceValue || 0),
        price: Number(body.price || 0),
        _skus: [], // 新增点卡时，初始 SKU 为空
        status: body.status === 'off' ? 'off' : 'on',
        validFrom: body.validFrom,
        validTo: body.validTo,
        description: body.description || '',
        createdAt: nowStr,
        updatedAt: nowStr
      };
      cards.unshift(card);
      return ok(card);
    }
  },
  {
    url: /\/api\/cards\/(\d+)$/,
    method: 'put',
    response: ({ body, url }) => {
      const id = Number(url?.match(/\/(\d+)$/)?.[1]);
      const cardIdx = cards.findIndex((c) => c.id === id);
      if (cardIdx === -1) return fail('点卡不存在', 404);

      const now = new Date();
      const nowStr = formatDateTimeToChineseTimezone(now);
      
      // 更新时，只修改传入的字段，_skus 不变
      cards[cardIdx] = { ...cards[cardIdx], ...body, updatedAt: nowStr };
      return ok(cards[cardIdx]);
    }
  },
  {
    url: /\/api\/cards\/(\d+)$/,
    method: 'delete',
    response: ({ url }) => {
      const id = Number(url?.match(/\/(\d+)$/)?.[1]);
      const idx = cards.findIndex((c) => c.id === id);
      if (idx === -1) return fail('Not Found', 404);
      const removed = cards.splice(idx, 1)[0];
      return ok(removed);
    }
  },
  {
    url: /\/api\/cards\/(\d+)\/status$/,
    method: 'patch',
    response: ({ url, body }) => {
      const id = Number(url?.match(/\/(\d+)\/status$/)?.[1]);
      const idx = cards.findIndex((c) => c.id === id);
      if (idx === -1) return fail('Not Found', 404);
      const next = body.status === 'off' ? 'off' : 'on';
      cards[idx].status = next;
      const now = new Date();
      cards[idx].updatedAt = formatDateTimeToChineseTimezone(now);
      return ok(cards[idx]);
    }
  },

  // 新增的库存管理接口
  {
    url: '/api/stock',
    method: 'post',
    response: ({ body }) => {
      const { cardCode, skuKey } = body;
      const card = cards.find(c => c.code === cardCode);
      if (!card) {
        return fail('点卡编号不存在', 1002);
      }
      if (card._skus.some(s => s.key === skuKey)) {
        return fail('SKU 密钥已存在', 1003);
      }
      card._skus.push({ key: skuKey, sold: false });
      const now = new Date();
      card.updatedAt = formatDateTimeToChineseTimezone(now);
      return ok({ message: 'SKU 添加成功' });
    }
  },

  // 新增的销售接口
  {
    url: '/api/sales',
    method: 'post',
    response: ({ body }) => {
      const { cardCode } = body;
      const card = cards.find(c => c.code === cardCode);
      if (!card) {
        return fail('点卡编号不存在', 1002);
      }
      const availableSKU = card._skus.find(s => !s.sold);
      if (!availableSKU) {
        return fail('库存不足', 1001);
      }

      availableSKU.sold = true;

      const now = new Date();
      const dealTime = formatDateTimeToChineseTimezone(now);
      
      // 同时生成订单，并添加到 orders mock 数据中
      const orderId = `ORD-${Date.now()}-${randInt(100, 999)}`;
      // 注意：这里需要导入 orders 数组，但 vite-plugin-mock 无法直接导入其他 mock 文件的数据
      // 在真实后端中，这里会调用 Order 服务来创建订单
      // 为了简化 mock，这里直接返回销售信息，不实际更新 orders 列表

      card.updatedAt = dealTime; // 更新点卡更新时间

      return ok({
        cardName: card.name,
        cardCode: card.code,
        skuKey: availableSKU.key,
        orderId: orderId,
        dealTime: dealTime,
      });
    }
  }
] as MockMethod[]; 