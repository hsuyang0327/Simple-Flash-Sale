export const dynamic = 'force-dynamic';

import JobTable from '@/components/admin/jobs/JobTable';
import { JobService } from '@/services/jobService';
import { Zap } from 'lucide-react';
import { JobResponse } from '@/types/job';

export default async function JobsPage() {
  let initialJobs: JobResponse[] = []; 
  
  try {
    initialJobs = await JobService.list();
  } catch (error) {
    console.error("Server fetch failed", error);
  }

  return (
    <div className="space-y-10">
      <div className="flex justify-between items-end border-b border-gray-100 pb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tighter text-gray-900">背景任務管理</h1>
          <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Quartz Scheduler Unit</p>
        </div>
        <div className="h-10 w-10 bg-gray-900 rounded-xl flex items-center justify-center text-white shadow-lg">
          <Zap size={18} fill="currentColor" />
        </div>
      </div>

      {/* 你的 UI 不動，只把資料傳進去 */}
      <JobTable initialJobs={initialJobs} />
    </div>
  );
}