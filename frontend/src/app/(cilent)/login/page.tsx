// 登入頁面初版 (src/app/(cilent)/login/page.tsx)
'use client';

import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { AuthService } from '@/services/authService';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  // 錯誤訊息交由 toast 處理，不再用 error state
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    // 不再需要 setError
    try {
      await AuthService.login(email, password);
      // 強制全頁跳轉，讓 Header 重新 fetch 登入狀態
      window.location.href = '/member';
    } catch (err) {
      // 錯誤已由 http.ts toast 處理
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md space-y-6 border border-gray-100">
        <h1 className="text-2xl font-bold text-center text-blue-700 mb-4">會員登入</h1>
        <div>
          <label className="block text-sm font-bold mb-2">電子郵件</label>
          <input
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
            className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="請輸入電子郵件"
            autoComplete="username"
            disabled={loading}
          />
        </div>
        <div>
          <label className="block text-sm font-bold mb-2">密碼</label>
          <div className="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 pr-12"
              placeholder="請輸入密碼"
              autoComplete="current-password"
              disabled={loading}
            />
            <button
              type="button"
              tabIndex={-1}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-blue-600"
              onClick={() => setShowPassword(v => !v)}
              aria-label={showPassword ? '隱藏密碼' : '顯示密碼'}
            >
              {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
        </div>
        {/* 錯誤訊息由 toast 處理，不再顯示紅字 */}
        <button
          type="submit"
          className="w-full py-3 bg-blue-600 text-white font-bold rounded-xl shadow-sm hover:bg-blue-700 transition-all disabled:opacity-50"
          disabled={loading}
        >
          {loading ? '登入中...' : '登入'}
        </button>
        <p className="text-center text-sm text-gray-500 mt-2">
          還沒有帳號？{' '}
          <a href="/register" className="text-blue-600 font-semibold hover:underline">立即註冊</a>
        </p>
      </form>
    </div>
  );
}
