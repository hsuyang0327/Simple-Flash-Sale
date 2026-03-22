import http from '@/lib/http';
import { MemberClientResponse, MemberUpdateRequest } from '@/types/member';

const BASE_PATH = '/client/member';

export const MemberClientService = {
  /**
   * 取得目前登入會員資料
   */
  me: (): Promise<MemberClientResponse> =>
    http.get(`${BASE_PATH}/me`),

  /**
   * 更新會員資料（姓名、密碼）
   */
  update: (payload: MemberUpdateRequest): Promise<MemberClientResponse> =>
    http.put(`${BASE_PATH}/me`, payload),
};
