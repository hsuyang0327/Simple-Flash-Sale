'use client';
import { JobResponse } from '@/types/job'; 
import { Activity, PauseCircle, AlertCircle } from 'lucide-react';

export default function JobStats({ jobs }: { jobs: JobResponse[] }) {
  // 對應你 Java 的狀態邏輯
  const activeJobs = jobs.filter(j => j.jobStatus === 'NORMAL').length;
  const pausedJobs = jobs.filter(j => j.jobStatus === 'PAUSED').length;
  const errorJobs = jobs.filter(j => j.jobStatus === 'ERROR' || j.jobStatus === 'BLOCKED').length;

  const stats = [
    { label: '運作中任務', value: activeJobs, icon: Activity, color: 'text-emerald-600', bg: 'bg-emerald-50' },
    { label: '已暫停', value: pausedJobs, icon: PauseCircle, color: 'text-amber-600', bg: 'bg-amber-50' },
    { label: '異常/阻塞', value: errorJobs, icon: AlertCircle, color: errorJobs > 0 ? 'text-rose-600' : 'text-gray-400', bg: errorJobs > 0 ? 'bg-rose-50' : 'bg-gray-50' },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {stats.map((stat) => (
        <div key={stat.label} className="bg-white border border-gray-100 p-6 rounded-[2.5rem] shadow-sm flex items-center gap-5">
          <div className={`w-12 h-12 ${stat.bg} rounded-2xl flex items-center justify-center ${stat.color}`}>
            <stat.icon size={22} />
          </div>
          <div>
            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">{stat.label}</p>
            <p className="text-2xl font-black font-mono text-gray-900 tabular-nums">{stat.value}</p>
          </div>
        </div>
      ))}
    </div>
  );
}