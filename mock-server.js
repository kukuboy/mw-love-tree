const http = require('http');

const PORT = 8080;

const mockData = {
  users: [
    { id: 1, email: 'test@test.com', password: '123456', nickname: '小明' },
    { id: 2, email: 'test2@test.com', password: '123456', nickname: '小红' },
  ],
  couples: [
    { id: 1, user1Id: 1, user2Id: 2, inviteCode: 'ABC123', anniversary: '2024-01-15', togetherDate: '2023-12-25' },
  ],
  events: [
    { id: 1, title: '第一次约会', date: '2023-12-25', description: '我们的第一次约会', type: 'date', createdAt: '2024-01-01T00:00:00Z' },
    { id: 2, title: '在一起纪念日', date: '2024-01-15', description: '正式在一起', type: 'anniversary', createdAt: '2024-01-15T00:00:00Z' },
  ],
  messages: [
    { id: 1, senderId: 2, content: '亲爱的，今天过得怎么样？', createdAt: '2024-01-20T10:00:00Z', read: false },
    { id: 2, senderId: 1, content: '很好呀，想你了', createdAt: '2024-01-20T10:05:00Z', read: true },
    { id: 3, senderId: 2, content: '我也想你，晚上一起吃饭吧', createdAt: '2024-01-20T10:10:00Z', read: false },
  ],
  photos: [
    { id: 1, url: 'https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=400', caption: '第一次旅行', createdAt: '2024-02-14T00:00:00Z' },
    { id: 2, url: 'https://images.unsplash.com/photo-1529333166437-7750a6dd5a70?w=400', caption: '情人节快乐', createdAt: '2024-02-14T12:00:00Z' },
  ],
  tree: {
    level: 3,
    growth: 45,
    lovePoints: 1250,
  },
  specialDays: [
    { id: 1, title: '在一起纪念日', date: '01-15', type: 'anniversary' },
    { id: 2, title: '情人节', date: '02-14', type: 'festival' },
  ],
};

function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  });
  res.end(JSON.stringify(data));
}

function parseBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        resolve({});
      }
    });
    req.on('error', reject);
  });
}

function success(data, message = 'success') {
  return { code: 200, message, data };
}

function error(message, code = 400) {
  return { code, message, data: null };
}

function getCoupleInfo(userId) {
  const couple = mockData.couples.find(c => c.user1Id === userId || c.user2Id === userId);
  if (!couple) return null;

  const partnerId = couple.user1Id === userId ? couple.user2Id : couple.user1Id;
  const partner = mockData.users.find(u => u.id === partnerId);
  const today = new Date();
  const together = new Date(couple.togetherDate);
  const daysTogether = Math.floor((today - together) / (1000 * 60 * 60 * 24));

  return {
    partnerId,
    partnerNickname: partner?.nickname || '未知',
    partnerAvatar: '',
    inviteCode: couple.inviteCode,
    anniversary: couple.anniversary,
    togetherDate: couple.togetherDate,
    daysTogether,
  };
}

const server = http.createServer(async (req, res) => {
  if (req.method === 'OPTIONS') {
    sendJson(res, 200, {});
    return;
  }

  const url = new URL(req.url, `http://localhost:${PORT}`);
  const pathname = url.pathname;
  const body = await parseBody(req);

  console.log(`[${req.method}] ${pathname}`);

  // ============ Auth APIs ============
  if (pathname === '/api/auth/login' && req.method === 'POST') {
    const user = mockData.users.find(u => u.email === body.email && u.password === body.password);
    if (!user) {
      sendJson(res, 401, error('邮箱或密码错误'));
      return;
    }
    const couple = mockData.couples.find(c => c.user1Id === user.id || c.user2Id === user.id);
    sendJson(res, 200, success({
      token: 'mock-jwt-token-' + user.id,
      userId: user.id,
      nickname: user.nickname,
      coupleId: couple?.id || null,
    }));
    return;
  }

  if (pathname === '/api/auth/register' && req.method === 'POST') {
    const existing = mockData.users.find(u => u.email === body.email);
    if (existing) {
      sendJson(res, 400, error('该邮箱已注册'));
      return;
    }
    const newId = mockData.users.length + 1;
    const newUser = {
      id: newId,
      email: body.email,
      password: body.password,
      nickname: body.nickname,
    };
    mockData.users.push(newUser);
    sendJson(res, 200, success({
      token: 'mock-jwt-token-' + newId,
      userId: newId,
      nickname: newUser.nickname,
      coupleId: null,
    }));
    return;
  }

  // ============ Couple APIs ============
  if (pathname === '/api/couple/invite' && req.method === 'POST') {
    const userId = 1;
    const couple = mockData.couples.find(c => c.user1Id === userId || c.user2Id === userId);
    sendJson(res, 200, success({ inviteCode: couple?.inviteCode || 'NEW123' }));
    return;
  }

  if (pathname === '/api/couple/join' && req.method === 'POST') {
    sendJson(res, 200, success({ token: 'mock-jwt-token-joined' }));
    return;
  }

  if (pathname === '/api/couple/info' && req.method === 'GET') {
    const info = getCoupleInfo(1);
    if (!info) {
      sendJson(res, 404, error('暂无伴侣信息'));
      return;
    }
    sendJson(res, 200, success(info));
    return;
  }

  if (pathname === '/api/couple/together-date' && req.method === 'PUT') {
    const couple = mockData.couples[0];
    if (couple) {
      couple.togetherDate = body.togetherDate;
    }
    sendJson(res, 200, success(null));
    return;
  }

  // ============ Events APIs ============
  if (pathname === '/api/events' && req.method === 'GET') {
    const page = parseInt(url.searchParams.get('page')) || 1;
    const size = parseInt(url.searchParams.get('size')) || 10;
    const start = (page - 1) * size;
    const items = mockData.events.slice(start, start + size);
    sendJson(res, 200, success({
      items,
      total: mockData.events.length,
      page,
      size,
    }));
    return;
  }

  if (pathname === '/api/events' && req.method === 'POST') {
    const newEvent = {
      id: mockData.events.length + 1,
      ...body,
      createdAt: new Date().toISOString(),
    };
    mockData.events.push(newEvent);
    sendJson(res, 200, success(newEvent));
    return;
  }

  const eventMatch = pathname.match(/^\/api\/events\/(\d+)$/);
  if (eventMatch) {
    const id = parseInt(eventMatch[1]);
    const event = mockData.events.find(e => e.id === id);
    if (!event) {
      sendJson(res, 404, error('事件不存在'));
      return;
    }
    if (req.method === 'GET') {
      sendJson(res, 200, success(event));
    } else if (req.method === 'PUT') {
      Object.assign(event, body);
      sendJson(res, 200, success(event));
    } else if (req.method === 'DELETE') {
      const idx = mockData.events.findIndex(e => e.id === id);
      if (idx > -1) mockData.events.splice(idx, 1);
      sendJson(res, 200, success(null));
    }
    return;
  }

  // ============ Photos APIs ============
  if (pathname === '/api/photos' && req.method === 'GET') {
    const page = parseInt(url.searchParams.get('page')) || 1;
    const size = parseInt(url.searchParams.get('size')) || 12;
    const start = (page - 1) * size;
    const items = mockData.photos.slice(start, start + size);
    sendJson(res, 200, success({
      items,
      total: mockData.photos.length,
      page,
      size,
    }));
    return;
  }

  if (pathname === '/api/photos' && req.method === 'POST') {
    const newPhoto = {
      id: mockData.photos.length + 1,
      url: body.url || 'https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=400',
      caption: body.caption || '',
      createdAt: new Date().toISOString(),
    };
    mockData.photos.push(newPhoto);
    sendJson(res, 200, success(newPhoto));
    return;
  }

  const photoMatch = pathname.match(/^\/api\/photos\/(\d+)$/);
  if (photoMatch && req.method === 'DELETE') {
    const id = parseInt(photoMatch[1]);
    const idx = mockData.photos.findIndex(p => p.id === id);
    if (idx > -1) mockData.photos.splice(idx, 1);
    sendJson(res, 200, success(null));
    return;
  }

  // ============ Messages APIs ============
  if (pathname === '/api/messages' && req.method === 'GET') {
    sendJson(res, 200, success(mockData.messages));
    return;
  }

  if (pathname === '/api/messages' && req.method === 'POST') {
    const newMsg = {
      id: mockData.messages.length + 1,
      senderId: 1,
      content: body.content,
      createdAt: new Date().toISOString(),
      read: false,
    };
    mockData.messages.push(newMsg);
    sendJson(res, 200, success(newMsg));
    return;
  }

  if (pathname === '/api/messages/unread-count' && req.method === 'GET') {
    const count = mockData.messages.filter(m => !m.read && m.senderId !== 1).length;
    sendJson(res, 200, success(count));
    return;
  }

  const msgReadMatch = pathname.match(/^\/api\/messages\/(\d+)\/read$/);
  if (msgReadMatch && req.method === 'PUT') {
    const id = parseInt(msgReadMatch[1]);
    const msg = mockData.messages.find(m => m.id === id);
    if (msg) msg.read = true;
    sendJson(res, 200, success(null));
    return;
  }

  // ============ Tree APIs ============
  if (pathname === '/api/tree' && req.method === 'GET') {
    sendJson(res, 200, success(mockData.tree));
    return;
  }

  if (pathname === '/api/tree/stats' && req.method === 'GET') {
    sendJson(res, 200, success({
      totalMessages: mockData.messages.length,
      totalEvents: mockData.events.length,
      totalPhotos: mockData.photos.length,
      daysTogether: getCoupleInfo(1)?.daysTogether || 0,
    }));
    return;
  }

  // ============ Special Days APIs ============
  if (pathname === '/api/special-days' && req.method === 'GET') {
    sendJson(res, 200, success(mockData.specialDays));
    return;
  }

  // ============ 404 ============
  sendJson(res, 404, error('API not found: ' + pathname));
});

server.listen(PORT, () => {
  console.log(`\n🌿 Mock API server running on http://localhost:${PORT}`);
  console.log(`📧 Test login: test@test.com / 123456\n`);
});
