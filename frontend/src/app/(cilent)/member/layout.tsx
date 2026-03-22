'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { User, ShoppingBag } from 'lucide-react';
import { MemberClientService } from '@/services/memberClientService';
import { MemberClientResponse } from '@/types/member';

export default function MemberLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [member, setMember] = useState<MemberClientResponse | null>(null);

  useEffect(() => {
    MemberClientService.me().then(setMember).catch(() => {});
  }, []);

  const menuItems = [
    { name: '個人資料', href: '/member', icon: User },
    { name: '我的訂單', href: '/member/orders', icon: ShoppingBag },
  ];

  return (
    <div className="grid grid-cols-10 gap-6 items-stretch min-h-[calc(100vh-130px)]">
      {/* Sidebar */}
      <aside className="col-span-4">
        <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-6 space-y-6 h-full">
          {/* 使用者資訊 */}
          <div className="space-y-1">
            <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-lg">
              {member?.memberName?.charAt(0).toUpperCase() ?? '?'}
            </div>
            <p className="font-bold text-gray-900 text-sm mt-3 truncate">{member?.memberName ?? '載入中...'}</p>
            <p className="text-xs text-gray-400 truncate">{member?.memberEmail ?? ''}</p>
          </div>

          <hr className="border-gray-100" />

          {/* 導覽選單 */}
          <nav className="space-y-1">
            {menuItems.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition-all ${
                    isActive
                      ? 'bg-blue-50 text-blue-600'
                      : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  <item.icon size={16} />
                  {item.name}
                </Link>
              );
            })}
          </nav>
        </div>
      </aside>

      {/* 主內容 */}
      <main className="col-span-6 min-w-0 h-full">{children}</main>
    </div>
  );
}
