import http from '@/lib/http';
import { JobResponse, JobRequest, JobCronRequest } from '@/types/job';

// 對應 @RequestMapping("/api/admin/jobs")
const BASE_PATH = '/admin/jobs';

export const JobService = {
    /**
     * 獲取所有任務 (@GetMapping)
     */
    list: (): Promise<JobResponse[]> =>
        http.get(BASE_PATH),

    /**
     * 暫停任務 (@PostMapping("/pause"))
     */
    pause: (data: JobRequest): Promise<void> =>
        http.post(`${BASE_PATH}/pause`, data),

    /**
     * 恢復任務 (@PostMapping("/resume"))
     */
    resume: (data: JobRequest): Promise<void> =>
        http.post(`${BASE_PATH}/resume`, data),

    /**
     * 立即執行 (@PostMapping("/trigger"))
     */
    trigger: (data: JobRequest): Promise<void> =>
        http.post(`${BASE_PATH}/trigger`, data),

    /**
     * 更新 Cron (@PostMapping("/cron"))
     */
    updateCron: (data: JobCronRequest): Promise<void> =>
        http.post(`${BASE_PATH}/cron`, data),

    /**
     * 手動預熱今日活動到 Redis (@PostMapping("/preload-today"))
     */
    preloadToday: (): Promise<void> =>
        http.post(`${BASE_PATH}/preload-today`),
};