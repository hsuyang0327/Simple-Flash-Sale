import http from '@/lib/http';
import { MemberAdminResponse, MemberAdminPageResponse } from '@/types/member';

// 對應 @RequestMapping("/api/admin/members")
const BASE_PATH = '/admin/members';

export const MemberAdminService = {
    /**
     * 獲取所有會員 (分頁) (@GetMapping)
     */
    list: (page: number = 0, size: number = 10): Promise<MemberAdminPageResponse> =>
        http.get(`${BASE_PATH}?page=${page}&size=${size}&sort=memberId&direction=DESC`),

    /**
     * 刪除會員 (@DeleteMapping("/{id}"))
     */
    delete: (id: string): Promise<void> =>
        http.delete(`${BASE_PATH}/${id}`),
};