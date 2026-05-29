'use client';

import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import Swal from 'sweetalert2';
import { AuthService } from '@/services/authService';

export default function RegisterPage() {
  const [memberName, setMemberName] = useState('');
  const [memberEmail, setMemberEmail] = useState('');
  const [memberPwd, setMemberPwd] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await AuthService.register(memberName, memberEmail, memberPwd);
      await Swal.fire({
        icon: 'success',
        title: '註冊成功！',
        text: '請使用您的帳號登入',
        confirmButtonText: '前往登入',
        confirmButtonColor: '#2563eb',
      });
      window.location.href = '/login';
    } catch {
      // errors handled by http.ts toast interceptor
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md space-y-6 border border-gray-100"
      >
        <h1 className="text-2xl font-bold text-center text-blue-700 mb-4">會員註冊</h1>

        <div>
          <label className="block text-sm font-bold mb-2">姓名</label>
          <input
            type="text"
            value={memberName}
            onChange={e => setMemberName(e.target.value)}
            required
            className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="請輸入姓名"
            autoComplete="name"
            disabled={loading}
          />
        </div>

        <div>
          <label className="block text-sm font-bold mb-2">電子郵件</label>
          <input
            type="email"
            value={memberEmail}
            onChange={e => setMemberEmail(e.target.value)}
            required
            className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="請輸入電子郵件"
            autoComplete="email"
            disabled={loading}
          />
        </div>

        <div>
          <label className="block text-sm font-bold mb-2">密碼</label>
          <div className="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              value={memberPwd}
              onChange={e => setMemberPwd(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 pr-12"
              placeholder="請輸入密碼"
              autoComplete="new-password"
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

        <button
          type="submit"
          className="w-full py-3 bg-blue-600 text-white font-bold rounded-xl shadow-sm hover:bg-blue-700 transition-all disabled:opacity-50"
          disabled={loading}
        >
          {loading ? '註冊中...' : '註冊'}
        </button>

        <p className="text-center text-sm text-gray-500 mt-2">
          已有帳號？{' '}
          <a href="/login" className="text-blue-600 font-semibold hover:underline">
            前往登入
          </a>
        </p>
      </form>
    </div>
  );
}
