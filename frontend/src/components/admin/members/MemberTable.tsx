'use client';

import { Trash2 } from 'lucide-react';
import { useState } from 'react';
import Swal from 'sweetalert2';
import { MemberAdminResponse, MemberAdminPageResponse } from '@/types/member';
import { MemberAdminService } from '@/services/memberAdminService';

interface MemberTableProps {
  initialMembers: MemberAdminPageResponse | null;
}

export default function MemberTable({ initialMembers }: MemberTableProps) {
  const [members, setMembers] = useState<MemberAdminResponse[]>(initialMembers?.content || []);
  const [currentPage, setCurrentPage] = useState(initialMembers?.page?.number || 0);
  const [totalPages, setTotalPages] = useState(initialMembers?.page?.totalPages || 0);
  const [totalElements, setTotalElements] = useState(initialMembers?.page?.totalElements || 0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(false);

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

  const loadPage = async (page: number) => {
    setLoading(true);
    try {
      const data = await MemberAdminService.list(page, pageSize);
      setMembers(data.content);
      setCurrentPage(data.page.number);
      setTotalPages(data.page.totalPages);
      setTotalElements(data.page.totalElements);
    } catch (error) {
      console.error("Failed to load member list:", error);
    } finally {
      setLoading(false);
    }
  };

  const refresh = async () => {
    await loadPage(currentPage);
  };

  const handleDelete = async (member: MemberAdminResponse) => {
    const result = await Swal.fire({
      title: '確認刪除',
      text: `確定要刪除會員 ${member.memberName} (${member.memberEmail}) 嗎？`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: '刪除',
      cancelButtonText: '取消',
      customClass: { popup: 'rounded-3xl' }
    });

    if (result.isConfirmed) {
      try {
        await MemberAdminService.delete(member.memberId);
        showSuccess('刪除成功', `會員 ${member.memberName} 已刪除`);
        await refresh();
      } catch (error) {
        console.error("Delete member failed:", error);
      }
    }
  };

  return (
    <div className="w-full space-y-8 p-4">

      <div className="bg-white border border-slate-100 rounded-[2.5rem] shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-separate border-spacing-0">
            <thead>
              <tr className="bg-slate-50/80">
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">電子郵件</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">姓名</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">創建時間</th>
                <th className="px-8 py-6 text-xs font-black text-slate-500 border-b border-slate-100 uppercase tracking-widest">更新時間</th>
                <th className="px-6 py-6 text-xs font-black text-slate-500 border-b border-slate-100 text-center uppercase tracking-widest">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {members.map((member, index) => (
                <tr key={member.memberId} className={`transition-all ${index % 2 === 0 ? 'bg-white' : 'bg-slate-50/30'} hover:bg-blue-50/40`}>
                  <td className="px-8 py-7">
                    <span className="text-sm text-slate-700 whitespace-nowrap">{member.memberEmail}</span>
                  </td>
                  <td className="px-8 py-7">
                    <span className="text-sm text-slate-700 whitespace-nowrap">{member.memberName}</span>
                  </td>
                  <td className="px-8 py-7">
                    <span className="text-sm text-slate-600 whitespace-nowrap">{new Date(member.createdAt).toLocaleString('zh-TW')}</span>
                  </td>
                  <td className="px-8 py-7">
                    <span className="text-sm text-slate-600 whitespace-nowrap">{new Date(member.updatedAt).toLocaleString('zh-TW')}</span>
                  </td>
                  <td className="px-6 py-7 text-center">
                    <button
                      onClick={() => handleDelete(member)}
                      className="p-2.5 bg-red-50 text-red-600 border border-red-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-red-100"
                      disabled={loading}
                    >
                      <Trash2 size={15} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 分頁控制 */}
      <div className="flex items-center justify-between bg-white border border-slate-100 rounded-[2.5rem] shadow-sm p-6">
        <div className="text-sm text-slate-600">
          顯示第 {currentPage * pageSize + 1} 到 {Math.min((currentPage + 1) * pageSize, totalElements)} 項，共 {totalElements} 項
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => loadPage(currentPage - 1)}
            disabled={currentPage === 0 || loading}
            className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一頁
          </button>
          <span className="px-4 py-2 text-sm font-bold text-slate-900">
            第 {currentPage + 1} 頁，共 {totalPages} 頁
          </span>
          <button
            onClick={() => loadPage(currentPage + 1)}
            disabled={currentPage >= totalPages - 1 || loading}
            className="px-4 py-2 bg-slate-50 text-slate-600 border border-slate-200 rounded-xl shadow-sm transition-all active:scale-90 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一頁
          </button>
        </div>
      </div>
    </div>
  );
}