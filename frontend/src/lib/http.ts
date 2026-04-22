import axios, { AxiosRequestConfig, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { toast } from 'react-hot-toast';
import Swal from 'sweetalert2';

export interface SilentAuthConfig extends InternalAxiosRequestConfig {
  _silentAuth?: boolean;
  _skipRefreshQueue?: boolean; // Prevents the refresh call itself from re-entering the refresh queue on 401
}

const http = axios.create({
  // 💡 自動判斷環境：Server 端連 8080，Client 端連 3000(透過 /api 轉發)
  baseURL: typeof window === 'undefined' 
    ? (process.env.INTERNAL_API_URL || 'http://127.0.0.1:8080/api')
    : '/api',
  timeout: 10000,
  withCredentials: true,
});
/**
 * 統一彈窗工具 (相容 Server/Client)
 */
const safeToast = {
  error: (msg: string) => {
    if (typeof window !== 'undefined') toast.error(msg);
    else console.error(`[API Error]: ${msg}`);
  },
  success: (msg: string) => {
    if (typeof window !== 'undefined') toast.success(msg);
  }
};
// 響應攔截器
let isRefreshing = false;
let hasSilentOnlyWaiters = true;  // false if any non-silent request is queued for refresh
let notLoggedIn = false;           // true if trigger was 4004 (no token) and no 4002 mixed in
let isShowingAuthPopup = false;
let refreshSubscribers: Array<(failed?: boolean) => void> = [];

function onRefreshed() {
  refreshSubscribers.forEach((cb) => cb());
  refreshSubscribers = [];
}

function onRefreshFailed() {
  refreshSubscribers.forEach((cb) => cb(true));
  refreshSubscribers = [];
}

function subscribeTokenRefresh(cb: (failed?: boolean) => void) {
  refreshSubscribers.push(cb);
}

function showAuthPopup(title: string, text: string) {
  if (isShowingAuthPopup) return;
  if (typeof window !== 'undefined' && window.location.pathname === '/login') return;
  isShowingAuthPopup = true;
  Swal.fire({
    title,
    text,
    icon: 'warning',
    confirmButtonText: '確定',
    customClass: { popup: 'rounded-3xl' },
  }).then(() => {
    isShowingAuthPopup = false;
    window.location.href = '/login';
  });
}

/**
 * Business error code handler — must be defined before the interceptor that calls it
 */
function handleBusinessError(code: number, message: string) {
  // 4609 STOCK_INVALID, 4620 STOCK_SOLD_OUT：由各頁面自行顯示更友善的提示，這裡不 toast
  if (code === 4609 || code === 4620) return;
  if (code >= 4500 && code <= 4599) {
    safeToast.error(`任務操作失敗: ${message}`);
  } else if (code === 5000) {
    safeToast.error("後端系統崩潰 (5000)");
  } else {
    safeToast.error(message || "操作異常");
  }
}

http.interceptors.response.use(
  (response) => {
    const { data: resData } = response;
    
    // 1. 處理標準 ApiResponse { code, data, message }
    if (resData && typeof resData.code === 'number') {
      if (resData.code === 200) {
        return resData.data;
      }
      // 2. 業務錯誤 (例如 4501 Job Not Found)
      handleBusinessError(resData.code, resData.message);
      return Promise.reject(new Error(resData.message));
    }

    // 3. 如果後端直接回傳陣列 (相容 listJobs 直接給 List)
    return resData;
  },
  (error) => {
    
    if (error.response) {
      const status = error.response.status;
      const resData = error.response.data;
      const bizCode: number | undefined = resData?.code;

      // 💡 後端回 HTTP 401，但 body 裡有 bizCode 才做 token 處理
      if (status === 401 && bizCode) {
        if (bizCode === 4002 || bizCode === 4004) { // ACCESS_TOKEN_EXPIRED or TOKEN_MISSING → 先嘗試 refresh
          // Guard: if this IS the refresh request, reject immediately to avoid infinite queue loop
          if ((error.config as SilentAuthConfig)?._skipRefreshQueue) {
            return Promise.reject(error);
          }
          const originalRequest = error.config;
          // Track if a non-silent request is waiting — determines whether popup shows on refresh failure
          if (!(error.config as SilentAuthConfig)?._silentAuth) {
            hasSilentOnlyWaiters = false;
            // 4002 = expired session (had token before); 4004 = never logged in
            // 4002 takes priority: if both appear, show "expired" not "not logged in"
            if (bizCode === 4004 && !notLoggedIn) notLoggedIn = true;
            if (bizCode === 4002) notLoggedIn = false;
          }
          if (!isRefreshing) {
            isRefreshing = true;
            // Always mark refresh itself as silent: popup is handled in .catch() below
            http.post('/client/auth/refresh', null, { _skipRefreshQueue: true, _silentAuth: true } as SilentAuthConfig)
              .then(() => {
                isRefreshing = false;
                hasSilentOnlyWaiters = true;
                notLoggedIn = false;
                onRefreshed();
              })
              .catch(() => {
                // Only show popup if at least one non-silent request was waiting
                if (!hasSilentOnlyWaiters) {
                  if (notLoggedIn) {
                    showAuthPopup('請先登入', '此操作需要登入後才能進行');
                  } else {
                    showAuthPopup('登入已過期', '請重新登入');
                  }
                }
                isRefreshing = false;
                hasSilentOnlyWaiters = true;
                notLoggedIn = false;
                onRefreshFailed();
              });
          }
          return new Promise((resolve, reject) => {
            subscribeTokenRefresh((failed?: boolean) => {
              if (failed) return reject(error);
              http(originalRequest).then(resolve).catch(reject);
            });
          });
        }

        if (bizCode === 4003) { // REFRESH_TOKEN_EXPIRED
          // silent request 不彈窗（例如 Header 的登入狀態檢查）
          if (!(error.config as SilentAuthConfig)?._silentAuth) {
            showAuthPopup('登入已過期', '請重新登入');
          }
          return Promise.reject(error);
        }

        if (bizCode === 4001) { // TOKEN_INVALID
          if (!(error.config as SilentAuthConfig)?._silentAuth) {
            showAuthPopup('權限失效', '請重新登入');
          }
          return Promise.reject(error);
        }
      }

      // 一般 HTTP 錯誤
      if (status === 404) {
        safeToast.error("找不到 API 路徑 (404)，請確認 next.config.mjs 的 rewrites 是否重啟");
      } else if (status !== 401) {
        safeToast.error(`伺服器錯誤 (${status}): ${resData?.message || '未知錯誤'}`);
      }
    } else {
      safeToast.error("系統連線異常，請檢查後端 Spring Boot 是否啟動");
    }
    return Promise.reject(error);
  }
);

// Override method return types to reflect interceptor behavior:
// the response interceptor unwraps ApiResponse<T>.data, so callers receive T directly.
type TypedHttp = Omit<AxiosInstance, 'get' | 'post' | 'put' | 'delete' | 'patch'> & {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>;
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>;
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>;
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>;
  patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>;
};

export default http as TypedHttp;