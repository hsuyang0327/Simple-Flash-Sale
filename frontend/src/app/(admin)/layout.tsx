'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import { ChevronLeft, ChevronRight, BarChart3, Package, ShoppingCart, User, Zap } from 'lucide-react';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [isCollapsed, setIsCollapsed] = useState(false);

  const menuItems = [
    { name: '總覽面板', href: '/dashboard', icon: BarChart3 },
    { name: '搶購商品', href: '/products', icon: Package }, // 暫留，可自行建立
    { name: '即時訂單', href: '/orders', icon: ShoppingCart },   // 暫留
    { name: '會員中心', href: '/members', icon: User },
    { name: '背景任務', href: '/jobs', icon: Zap },
  ];

  return (
    <div className="flex min-h-screen bg-[#F9FAFB] text-[#111827] font-sans selection:bg-blue-50">
      {/* 側邊欄 */}
      <aside className={`h-screen sticky top-0 bg-white border-r border-gray-200 flex flex-col transition-all duration-300 ${isCollapsed ? 'w-16' : 'w-64'}`}>
        <div className={`h-16 flex items-center ${isCollapsed ? 'justify-center px-2' : 'justify-between px-4'} border-b border-gray-50`}>
          {!isCollapsed && (
            <div className="flex items-center">
              <span className="w-6 h-6 bg-blue-600 rounded-md shadow-sm mr-3 flex items-center justify-center text-white font-bold text-sm">S</span>
              <span className="font-bold tracking-tight text-sm uppercase">Simple Flash</span>
            </div>
          )}
          <button
            onClick={() => setIsCollapsed(!isCollapsed)}
            className="p-1 rounded-md hover:bg-gray-100 transition-colors"
          >
            {isCollapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
          </button>
        </div>
        
        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link 
                key={item.href} 
                href={item.href} 
                className={`flex items-center ${isCollapsed ? 'px-3 justify-center' : 'px-4'} py-2.5 rounded-xl text-xs font-semibold transition-all ${
                  isActive ? 'bg-gray-50 text-blue-600' : 'text-gray-500 hover:bg-gray-50/50 hover:text-gray-900'
                }`}
                title={isCollapsed ? item.name : undefined}
              >
                <item.icon size={16} className={`${isCollapsed ? '' : 'mr-3'}`} />
                {!isCollapsed && item.name}
              </Link>
            );
          })}
        </nav>

        <div className={`${isCollapsed ? 'p-4' : 'p-6'} border-t border-gray-50`}>
          <div className="flex items-center gap-3 px-2">
            <div className="w-8 h-8 rounded-full bg-gray-100 border border-gray-200"></div>
            {!isCollapsed && (
              <div className="text-[10px]">
                <p className="font-bold tracking-tight">System Admin</p>
                <p className="text-gray-400 font-mono">ID: 0327</p>
              </div>
            )}
          </div>
        </div>
      </aside>

      <main className="flex-1 overflow-y-auto">
        {/* 頂部導航 */}
        <header className="h-16 bg-white/80 backdrop-blur-md border-b border-gray-200/50 sticky top-0 z-10 flex items-center justify-between px-10">
          <h2 className="text-[10px] font-black uppercase tracking-[0.3em] text-gray-400">
            {pathname === '/jobs' ? 'Task Orchestrator' : 'System Dashboard'}
          </h2>
          <div className="flex items-center gap-4 text-[10px] font-bold text-gray-400">
            <span className="flex items-center gap-1.5">
              <span className="w-1 h-1 bg-emerald-500 rounded-full"></span>
              NODE_STABLE
            </span>
          </div>
        </header>
        
        {/* 頁面內容：這裡統一控制最大寬度，確保所有頁面比例一致 */}
        <div className="p-10 max-w-6xl mx-auto">
          {children}
          <Toaster position="top-center" reverseOrder={false} />
        </div>
      </main>
    </div>
  );
}