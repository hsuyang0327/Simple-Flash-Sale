'use client';
import { useState, useEffect } from 'react';
import { DashboardService } from '@/services/dashboardService';

export default function RedisDelay() {
  const [delay, setDelay] = useState<number | null>(null);

  const measure = async () => {
    const t1 = Date.now();
    try {
      await DashboardService.getStocks();
      setDelay(Date.now() - t1);
    } catch {
      // 若後端未啟動則不更新
    }
  };

  useEffect(() => {
    measure();
    const timer = setInterval(measure, 10000);
    return () => clearInterval(timer);
  }, []);

  return (
    <span className="text-sm font-mono font-bold tabular-nums">
      {delay === null ? '…' : `${delay}ms`}
    </span>
  );
}