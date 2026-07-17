import { describe, it, expect, vi, beforeAll, beforeEach, afterEach } from 'vitest';
import MockAdapter from 'axios-mock-adapter';
import type { AxiosInstance } from 'axios';

vi.mock('react-hot-toast', () => ({
  toast: { error: vi.fn(), success: vi.fn() },
}));

vi.mock('sweetalert2', () => ({
  default: { fire: vi.fn(() => Promise.resolve()) },
}));

let http: AxiosInstance;
let mock: MockAdapter;

beforeAll(() => {
  // Swal.fire().then() sets window.location.href = '/login'; stub location so
  // jsdom doesn't attempt a real (unsupported) navigation during that callback.
  Object.defineProperty(window, 'location', {
    writable: true,
    value: { pathname: '/', href: '' },
  });
});

beforeEach(async () => {
  // http.ts keeps module-level state (isRefreshing, notLoggedIn, ...) as closures,
  // so each test needs a fresh module instance to avoid cross-test leakage.
  vi.resetModules();
  const mod = await import('./http');
  http = mod.default as unknown as AxiosInstance;
  mock = new MockAdapter(http);
});

afterEach(() => {
  mock.restore();
  vi.clearAllMocks();
});

describe('http.ts refresh queue', () => {
  it('並發多請求同時收到 401(4002) 時，只呼叫一次 refresh，成功後全部請求重放成功', async () => {
    mock.onGet('/protected-a').replyOnce(401, { code: 4002, message: 'Access token expired' });
    mock.onGet('/protected-a').replyOnce(200, { code: 200, data: 'a-ok' });
    mock.onGet('/protected-b').replyOnce(401, { code: 4002, message: 'Access token expired' });
    mock.onGet('/protected-b').replyOnce(200, { code: 200, data: 'b-ok' });
    mock.onPost('/client/auth/refresh').replyOnce(200, { code: 200, data: null });

    const [a, b] = await Promise.all([http.get('/protected-a'), http.get('/protected-b')]);

    expect(a).toBe('a-ok');
    expect(b).toBe('b-ok');
    const refreshCalls = mock.history.post!.filter((c) => c.url === '/client/auth/refresh');
    expect(refreshCalls.length).toBe(1);
  });

  it('refresh 失敗時，所有排隊中的請求都 reject，且非 silent 請求會觸發彈窗', async () => {
    mock.onGet('/protected-a').reply(401, { code: 4002, message: 'expired' });
    mock.onGet('/protected-b').reply(401, { code: 4002, message: 'expired' });
    mock.onPost('/client/auth/refresh').reply(401, { code: 4003, message: 'refresh expired' });

    const results = await Promise.allSettled([http.get('/protected-a'), http.get('/protected-b')]);

    expect(results[0].status).toBe('rejected');
    expect(results[1].status).toBe('rejected');

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).toHaveBeenCalledTimes(1);
    expect(Swal.fire).toHaveBeenCalledWith(expect.objectContaining({ title: '登入已過期' }));
  });

  it('全部排隊請求皆為 _silentAuth 時，refresh 失敗不會觸發彈窗', async () => {
    mock.onGet('/silent-check').reply(401, { code: 4002, message: 'expired' });
    mock.onPost('/client/auth/refresh').reply(401, { code: 4003, message: 'refresh expired' });

    await expect(
      http.get('/silent-check', { _silentAuth: true } as never)
    ).rejects.toBeTruthy();

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).not.toHaveBeenCalled();
  });

  it('_skipRefreshQueue 為 true 的請求（refresh 呼叫本身）收到 401 時直接 reject，不會再觸發一次 refresh', async () => {
    mock.onPost('/client/auth/refresh').reply(401, { code: 4002, message: 'expired' });

    await expect(
      http.post('/client/auth/refresh', null, {
        _skipRefreshQueue: true,
        _silentAuth: true,
      } as never)
    ).rejects.toBeTruthy();

    const refreshCalls = mock.history.post!.filter((c) => c.url === '/client/auth/refresh');
    expect(refreshCalls.length).toBe(1);
  });

  it('bizCode 4004（從未登入）時，refresh 失敗彈窗文案為「請先登入」', async () => {
    mock.onGet('/needs-login').reply(401, { code: 4004, message: 'no token' });
    mock.onPost('/client/auth/refresh').reply(401, { code: 4003, message: 'refresh expired' });

    await expect(http.get('/needs-login')).rejects.toBeTruthy();

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).toHaveBeenCalledWith(expect.objectContaining({ title: '請先登入' }));
  });

  it('先收到 4004 再混入 4002 時，最終彈窗文案改為「登入已過期」（4002 優先於 4004）', async () => {
    mock.onGet('/a').reply(401, { code: 4004, message: 'no token' });
    mock.onGet('/b').reply(401, { code: 4002, message: 'expired' });
    mock.onPost('/client/auth/refresh').reply(401, { code: 4003, message: 'refresh expired' });

    const pA = http.get('/a').catch(() => undefined);
    // 讓 /a 的 401 攔截器同步邏輯（設定 notLoggedIn=true、啟動 refresh）先跑完，
    // 再送出 /b，確保 4002 的覆蓋邏輯在 4004 之後執行。
    await new Promise((resolve) => setTimeout(resolve, 0));
    const pB = http.get('/b').catch(() => undefined);
    await Promise.all([pA, pB]);

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).toHaveBeenCalledWith(expect.objectContaining({ title: '登入已過期' }));
  });

  it('bizCode 4003（REFRESH_TOKEN_EXPIRED）直接發生時，非 silent 立即彈窗，且不會觸發 refresh', async () => {
    mock.onGet('/direct-4003').reply(401, { code: 4003, message: 'refresh expired' });

    await expect(http.get('/direct-4003')).rejects.toBeTruthy();

    const refreshCalls = mock.history.post!.filter((c) => c.url === '/client/auth/refresh');
    expect(refreshCalls.length).toBe(0);

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).toHaveBeenCalledWith(expect.objectContaining({ title: '登入已過期' }));
  });

  it('_silentAuth 請求收到 bizCode 4003 時不會彈窗', async () => {
    mock.onGet('/silent-4003').reply(401, { code: 4003, message: 'refresh expired' });

    await expect(
      http.get('/silent-4003', { _silentAuth: true } as never)
    ).rejects.toBeTruthy();

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).not.toHaveBeenCalled();
  });

  it('bizCode 4001（TOKEN_INVALID）時，非 silent 立即彈窗「權限失效」，且不會觸發 refresh', async () => {
    mock.onGet('/direct-4001').reply(401, { code: 4001, message: 'invalid token' });

    await expect(http.get('/direct-4001')).rejects.toBeTruthy();

    const refreshCalls = mock.history.post!.filter((c) => c.url === '/client/auth/refresh');
    expect(refreshCalls.length).toBe(0);

    const Swal = (await import('sweetalert2')).default;
    expect(Swal.fire).toHaveBeenCalledWith(expect.objectContaining({ title: '權限失效' }));
  });

  it('HTTP 200 但 code !== 200 時視為業務錯誤，reject 並透過 handleBusinessError 顯示 toast', async () => {
    mock.onGet('/biz-error').reply(200, { code: 4501, message: 'Job Not Found' });

    await expect(http.get('/biz-error')).rejects.toThrow('Job Not Found');

    const { toast } = await import('react-hot-toast');
    expect(toast.error).toHaveBeenCalledWith('任務操作失敗: Job Not Found');
  });

  it('bizCode 4609/4620（庫存相關業務錯誤）不會觸發全域 toast，交由頁面自行處理', async () => {
    mock.onGet('/stock-sold-out').reply(200, { code: 4620, message: 'Sold out' });

    await expect(http.get('/stock-sold-out')).rejects.toThrow('Sold out');

    const { toast } = await import('react-hot-toast');
    expect(toast.error).not.toHaveBeenCalled();
  });
});
