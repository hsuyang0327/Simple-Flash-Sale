'use client';

import { useEffect, useState, useCallback } from 'react';
import RedisDelay from '@/components/admin/dashboard/RedisDelay';
import { DashboardService } from '@/services/dashboardService';
import { DashboardStockResponse } from '@/types/dashboard';

export default function DashboardPage() {
    const [stocks, setStocks] = useState<DashboardStockResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const loadStocks = useCallback(async (isRefresh = false) => {
        if (isRefresh) setRefreshing(true);
        try {
            const data = await DashboardService.getStocks();
            setStocks(data);
        } catch {
            // 錯誤由 http.ts toast 處理
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, []);

    useEffect(() => {
        loadStocks();
    }, [loadStocks]);

    return (
        <div className="space-y-10">
            {/* 頂部標題 */}
            <div className="flex justify-between items-end border-b border-gray-100 pb-8">
                <div>
                    <h1 className="text-2xl font-bold tracking-tighter text-gray-900">Redis 庫存監控</h1>
                    <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Real-time Stocks</p>
                </div>
                <div className="flex items-center gap-3 mb-1">
                    <button
                        onClick={() => loadStocks(true)}
                        disabled={refreshing}
                        className="flex items-center gap-1.5 px-3 py-1 text-[10px] font-black uppercase tracking-widest text-blue-600 bg-blue-50 border border-blue-100 rounded-full hover:bg-blue-100 transition-all disabled:opacity-40"
                    >
                        {refreshing ? '更新中...' : '↻ 重新整理'}
                    </button>
                    <div className="flex items-center gap-2 px-3 py-1 bg-emerald-50 rounded-full border border-emerald-100">
                        <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-pulse"></span>
                        <span className="text-[10px] font-black text-emerald-700 font-mono tracking-tight uppercase">Connected</span>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* 左側：監控列表 (佔 2 欄) */}
                <div className="lg:col-span-2 space-y-4">
                    {loading ? (
                        <div className="text-sm text-gray-400 py-10 text-center">載入中...</div>
                    ) : stocks.length === 0 ? (
                        <div className="text-sm text-gray-400 py-10 text-center">Redis 目前無預熱商品</div>
                    ) : (
                        stocks.map((item) => {
                            const isSoldOut = item.redisStock === 0 && item.dbStock === 0;
                            const isOutOfSync = item.redisStock !== item.dbStock;

                            let statusLabel = 'STABLE';
                            let dotColor = 'bg-emerald-500';
                            let statusColor = 'text-emerald-600';
                            if (isSoldOut) {
                                statusLabel = 'SOLD OUT';
                                dotColor = 'bg-gray-300';
                                statusColor = 'text-gray-400';
                            } else if (isOutOfSync) {
                                statusLabel = 'PENDING';
                                dotColor = 'bg-amber-400';
                                statusColor = 'text-amber-600';
                            }

                            return (
                                <div key={item.productId} className="bg-white border border-gray-100 p-6 rounded-[2rem] shadow-sm hover:border-blue-400 hover:shadow-md transition-all">
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <span className="text-[10px] font-mono text-gray-400 font-bold uppercase tracking-tighter">{item.productId.slice(0, 8)}</span>
                                            <h3 className="text-lg font-bold text-gray-800 tracking-tight">{item.productName}</h3>
                                        </div>
                                        <div className="flex items-center gap-1.5">
                                            <span className={`w-2 h-2 rounded-full ${dotColor} ${!isSoldOut && isOutOfSync ? 'animate-pulse' : ''}`}></span>
                                            <span className={`text-[10px] font-black uppercase tracking-widest ${statusColor}`}>{statusLabel}</span>
                                        </div>
                                    </div>

                                    <div className="mt-5 flex items-end gap-8">
                                        <div>
                                            <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1">Redis 庫存</p>
                                            <span className={`text-3xl font-black font-mono tabular-nums ${isSoldOut ? 'text-gray-300' : 'text-blue-600'}`}>
                                                {item.redisStock}
                                            </span>
                                        </div>
                                        <div className="text-gray-200 text-2xl font-thin mb-1">|</div>
                                        <div>
                                            <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1">DB 庫存</p>
                                            <span className={`text-3xl font-black font-mono tabular-nums ${isSoldOut ? 'text-gray-300' : isOutOfSync ? 'text-amber-500' : 'text-gray-700'}`}>
                                                {item.dbStock}
                                            </span>
                                        </div>
                                        {isOutOfSync && !isSoldOut && (
                                            <div className="mb-1">
                                                <p className="text-[9px] font-black text-amber-500 uppercase tracking-widest mb-1">差距</p>
                                                <span className="text-3xl font-black font-mono tabular-nums text-amber-500">
                                                    {Math.abs(item.redisStock - item.dbStock)}
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>

                {/* 右側：功能卡片區 (佔 1 欄) */}
                <div className="space-y-6">
                    {/* GitHub 卡片 */}
                    <a
                        href="https://github.com/YourAccount/YourProject"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="block bg-slate-800 p-8 rounded-[2.5rem] border border-slate-700 text-white hover:bg-slate-700 hover:border-blue-500/50 hover:shadow-[0_20px_40px_-15px_rgba(30,41,59,0.4)] transition-all duration-500 group relative overflow-hidden"
                    >
                        <div className="absolute -right-10 -bottom-10 w-32 h-32 bg-blue-500/10 rounded-full group-hover:scale-150 transition-transform duration-1000"></div>
                        <div className="relative z-10">
                            <div className="w-10 h-10 bg-white/5 rounded-xl flex items-center justify-center mb-6 border border-white/10 group-hover:border-white/20 group-hover:bg-white/10 transition-all duration-500">
                                <svg className="w-6 h-6 fill-white/90" viewBox="0 0 24 24"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" /></svg>
                            </div>
                            <h4 className="text-sm font-bold tracking-tight mb-2 text-white">Source Code</h4>
                            <p className="text-[10px] text-slate-400 leading-relaxed font-medium">查看系統後端邏輯與 Quartz 任務實作細節。</p>
                            <div className="mt-8 flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-blue-400 group-hover:text-blue-300 transition-colors">
                                View on GitHub <span>→</span>
                            </div>
                        </div>
                    </a>

                    {/* Redis Delay 卡片 */}
                    <div className="bg-white border border-gray-100 p-8 rounded-[2rem] shadow-sm flex items-center justify-between group hover:border-blue-400 transition-all cursor-default relative overflow-hidden">
                        <div className="relative z-10">
                            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest block mb-1">Response Delay</span>
                            <span className="text-xs font-bold text-gray-800">Redis API</span>
                        </div>
                        <div className="relative z-10 scale-110">
                            <RedisDelay />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}