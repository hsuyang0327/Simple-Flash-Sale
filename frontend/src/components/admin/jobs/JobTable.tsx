'use client';

import { Play, Pause, Zap, Edit3, Calendar, CheckCircle2, AlertCircle, List, Flame } from 'lucide-react';
import { useState } from 'react';
import Swal from 'sweetalert2';
import toast from 'react-hot-toast';
import cronstrue from 'cronstrue/i18n';
import { JobResponse, JobRequest } from '@/types/job';
import { JobService } from '@/services/jobService';
import EditCronModal from './EditCronModal';

interface JobTableProps {
  initialJobs: JobResponse[];
}

export default function JobTable({ initialJobs = [] }: JobTableProps) {
  const [jobs, setJobs] = useState<JobResponse[]>(initialJobs);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedJob, setSelectedJob] = useState<JobResponse | null>(null);
  const [isPreloading, setIsPreloading] = useState(false);

  // 封裝統一的 SweetAlert2 成功彈窗
  const showSuccess = (title: string, text: string) => {
    Swal.fire({
      title,
      text,
      icon: 'success',
      timer: 2000,
      showConfirmButton: false,
      timerProgressBar: true,
      customClass: { popup: 'rounded-3xl' }
    });
  };

  const refresh = async () => {
    try {
      const data = await JobService.list();
      setJobs(data);
    } catch (error) {
      console.error("Failed to refresh job list:", error);
    }
  };

  const stats = {
    total: jobs.length,
    normal: jobs.filter(j => j.jobStatus === 'NORMAL').length,
    paused: jobs.filter(j => j.jobStatus === 'PAUSED').length
  };
  const getCronChinese = (cron: string | null | undefined) => {
    if (!cron || cron.trim() === "") {
      return "目前無排程";
    }
    try {
      return cronstrue.toString(cron, { locale: "zh_TW", use24HourTimeFormat: true });
    } catch (e) {
      console.error(`Cron parse error for: ${cron}`, e);
      return "格式解析錯誤";
    }
  };
  const toggleStatus = async (job: JobResponse) => {
    const payload: JobRequest = { jobName: job.jobName, jobGroup: job.jobGroup };
    try {
      if (job.jobStatus === 'NORMAL') {
        await JobService.pause(payload);
        showSuccess('已暫停', `任務 [${job.jobName}] 已成功暫停`);
      } else {
        await JobService.resume(payload);
        showSuccess('已恢復', `任務 [${job.jobName}] 已成功恢復`);
      }
      await refresh();
    } catch (error) {
      console.error("Toggle status failed:", error);
    }
  };

  const handleTrigger = async (job: JobResponse) => {
    try {
      await JobService.trigger({ jobName: job.jobName, jobGroup: job.jobGroup });
      showSuccess('已觸發', `任務 [${job.jobName}] 執行訊號已發送`);
    } catch (error) {
      console.error("Manual trigger failed:", error);
    }
  };

  const handlePreloadToday = async () => {
    setIsPreloading(true);
    try {
      await JobService.preloadToday();
      toast.success('今日活動已預熱至 Redis');
    } catch (error) {
      console.error('Preload today failed:', error);
    } finally {
      setIsPreloading(false);
    }
  };

  const onUpdateCron = async (newCron: string) => {
    if (!selectedJob) return;
    try {
      await JobService.updateCron({
        jobName: selectedJob.jobName,
        jobGroup: selectedJob.jobGroup,
        cronExpression: newCron
      });
      showSuccess('更新成功', 'Cron 排程已更新');
      setIsModalOpen(false);
      await refresh();
    } catch (error) {
      console.error("Update cron failed:", error);
    }
  };

  return (
    <div className="w-full space-y-8 p-4">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white p-6 rounded-[2.5rem] border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center">
            <List size={24} />
          </div>
          <div>
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">任務總數</p>
            <p className="text-2xl font-black text-slate-900">{stats.total}</p>
          </div>
        </div>
        <div className="bg-white p-6 rounded-[2.5rem] border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center">
            <CheckCircle2 size={24} />
          </div>
          <div>
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">正常運行</p>
            <p className="text-2xl font-black text-emerald-600">{stats.normal}</p>
          </div>
        </div>
        <div className="bg-white p-6 rounded-[2.5rem] border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center">
            <AlertCircle size={24} />
          </div>
          <div>
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest text-amber-500">已暫停</p>
            <p className="text-2xl font-black text-amber-600">{stats.paused}</p>
          </div>
        </div>
        <button
          onClick={handlePreloadToday}
          disabled={isPreloading}
          className="bg-white p-6 rounded-[2.5rem] border border-slate-100 shadow-sm flex items-center gap-4 hover:border-orange-200 hover:bg-orange-50/50 transition-all active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed w-full text-left"
        >
          <div className="w-12 h-12 bg-orange-50 text-orange-500 rounded-2xl flex items-center justify-center shrink-0">
            <Flame size={24} />
          </div>
          <div>
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">測試工具</p>
            <p className="text-sm font-black text-orange-500">{isPreloading ? '預熱中...' : '預熱今日活動'}</p>
          </div>
        </button>
      </div>

      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-separate border-spacing-0">
            <thead>
              <tr className="bg-slate-50/80">
                <th className="px-10 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">任務詳情</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest text-left">執行排程 (Cron)</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 text-center uppercase tracking-widest">任務狀態</th>
                <th className="px-6 py-6 text-xs font-black text-slate-500 border-b border-slate-100 text-center uppercase tracking-widest">操作面板</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {jobs.map((job, index) => (
                <tr key={job.jobName} className={`transition-all ${index % 2 === 0 ? 'bg-white' : 'bg-slate-50/30'} hover:bg-blue-50/40`}>
                  <td className="px-10 py-7">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="px-2 py-0.5 bg-slate-100 text-slate-500 text-[9px] font-black rounded border border-slate-200 uppercase tracking-tighter">{job.jobGroup}</span>
                        <span className="text-sm font-bold text-slate-900 tracking-tight">{job.jobName}</span>
                      </div>
                      <p className="text-xs text-slate-400 font-medium truncate max-w-[200px]">{job.description}</p>
                    </div>
                  </td>
                  <td className="px-8 py-7">
                    <div className="space-y-2">
                      <div className="flex items-center gap-3">
                        <span className="font-mono text-[11px] font-bold text-blue-700 bg-blue-50 border border-blue-100 px-2.5 py-1 rounded-lg">{job.cronExpression}</span>
                        <span className="text-sm font-black text-slate-700">{getCronChinese(job.cronExpression)}</span>
                      </div>
                      <div className="flex items-center gap-1.5 text-slate-400 font-medium text-[10px] ml-1">
                        <Calendar size={12} className="text-slate-300" />
                        下次執行：<span className="text-slate-600 font-bold">{job.nextFireTime || 'N/A'}</span>
                      </div>
                    </div>
                  </td>
                  <td className="px-8 py-7 text-center">
                    <span className={`inline-flex items-center px-4 py-1.5 rounded-full text-[10px] font-black border shadow-sm ${job.jobStatus === 'NORMAL' ? 'bg-emerald-500 text-white border-emerald-600' : 'bg-amber-500 text-white border-amber-600'}`}>
                      {job.jobStatus}
                    </span>
                  </td>
                  <td className="px-6 py-7 text-center">
                    <div className="flex justify-center gap-2.5">
                      <button onClick={() => toggleStatus(job)} className={`p-2.5 rounded-xl border transition-all active:scale-90 shadow-sm ${job.jobStatus === 'NORMAL' ? 'bg-amber-50 text-amber-600 border-amber-200 hover:bg-amber-100' : 'bg-emerald-50 text-emerald-600 border-emerald-200 hover:bg-emerald-100'}`}>
                        {job.jobStatus === 'NORMAL' ? <Pause size={15} fill="currentColor" /> : <Play size={15} fill="currentColor" />}
                      </button>
                      <button onClick={() => handleTrigger(job)} className="p-2.5 bg-blue-50 text-blue-600 border border-blue-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-blue-100">
                        <Zap size={15} fill="currentColor" />
                      </button>
                      <button onClick={() => { setSelectedJob(job); setIsModalOpen(true); }} className="p-2.5 bg-slate-50 text-slate-500 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100">
                        <Edit3 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <EditCronModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        jobName={selectedJob?.jobName || ''}
        currentCron={selectedJob?.cronExpression || ''}
        onSave={onUpdateCron}
      />
    </div>
  );
}