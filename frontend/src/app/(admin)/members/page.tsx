import MemberTable from '@/components/admin/members/MemberTable';
import { MemberAdminService } from '@/services/memberAdminService';
import { User } from 'lucide-react';
import { MemberAdminPageResponse } from '@/types/member';

export default async function MembersPage() {
  let initialMembers: MemberAdminPageResponse | null = null;

  try {
    initialMembers = await MemberAdminService.list(0, 10);
  } catch (error) {
    console.error("Server fetch failed", error);
    initialMembers = {
      content: [],
      page: { size: 10, number: 0, totalElements: 0, totalPages: 0 },
    };
  }
  return (
    <div className="space-y-10">
      <div className="flex justify-between items-end border-b border-gray-100 pb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tighter text-gray-900">會員管理</h1>
          <p className="text-xs font-medium text-gray-400 mt-1 uppercase tracking-widest italic font-mono">Member Management Unit</p>
        </div>
        <div className="h-10 w-10 bg-gray-900 rounded-xl flex items-center justify-center text-white shadow-lg">
          <User size={18} fill="currentColor" />
        </div>
      </div>

      <MemberTable initialMembers={initialMembers} />
    </div>
  );
}