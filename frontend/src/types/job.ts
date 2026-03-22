export interface JobResponse {
  jobName: string;
  jobGroup: string;
  jobStatus: 'NONE' | 'NORMAL' | 'PAUSED' | 'COMPLETE' | 'ERROR' | 'BLOCKED';
  cronExpression: string;
  previousFireTime: string | null;
  nextFireTime: string | null;
  description: string;
}

export interface JobRequest {
  jobName: string;
  jobGroup: string;
}

export interface JobCronRequest extends JobRequest {
  cronExpression: string;
}