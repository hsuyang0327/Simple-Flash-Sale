"use client";
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { User, ShoppingBag, LogOut } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { AuthService } from '@/services/authService';
import http from '@/lib/http';
import { SilentAuthConfig } from '@/lib/http';
import { MemberClientResponse } from '@/types/member';

async function fetchUser() {
  try {
    const data = await http.get<MemberClientResponse>('/client/member/me', { _silentAuth: true } as SilentAuthConfig);
    return data?.memberId ? { memberId: data.memberId, memberName: data.memberName } : null;
  } catch {
    return null;
  }
}

export default function Header() {
  const [user, setUser] = useState<null | { memberId: string; memberName: string }>(null);
  const router = useRouter();

  useEffect(() => {
    fetchUser().then(setUser);
  }, []);

  const handleLogout = async () => {
    try {
      await AuthService.logout();
      window.location.href = '/home';
    } catch (e) {
      // 錯誤已由 http.ts 處理
    }
  };

  return (
    <header className="w-full bg-white border-b border-gray-100 shadow-sm px-6 py-4 flex items-center justify-between">
      <div className="flex items-center gap-6">
        <Link href="/" className="flex items-center gap-2 font-bold text-lg text-blue-700">
          <ShoppingBag size={20} />
          商品
        </Link>
      </div>
      <nav className="flex items-center gap-6">
        {!user ? (
          <Link href="/login" className="text-gray-700 font-bold hover:text-blue-600 transition-colors">登入</Link>
        ) : (
          <>
            <Link href="/member" className="flex items-center gap-2 text-gray-700 font-bold hover:text-blue-600 transition-colors">
              <User size={18} />會員專區
            </Link>
            <button
              onClick={handleLogout}
              className="flex items-center gap-1 text-gray-500 font-bold hover:text-red-600 transition-colors px-2 py-1 rounded-xl border border-transparent hover:border-red-200"
              title="登出"
              type="button"
            >
              <LogOut size={18} />登出
            </button>
          </>
        )}
      </nav>
    </header>
  );
}
