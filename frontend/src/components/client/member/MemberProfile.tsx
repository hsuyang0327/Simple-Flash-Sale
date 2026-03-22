'use client';

import { useEffect, useState } from 'react';
import { Pencil, X, Check } from 'lucide-react';
import Swal from 'sweetalert2';
import { MemberClientService } from '@/services/memberClientService';
import { MemberClientResponse } from '@/types/member';

export default function MemberProfile() {
  const [member, setMember] = useState<MemberClientResponse | null>(null);
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState('');
  const [pwd, setPwd] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    MemberClientService.me().then((data) => {
      setMember(data);
      setName(data.memberName);
    }).catch(() => {});
  }, []);

  const handleSave = async () => {
    setLoading(true);
    try {
      const updated = await MemberClientService.update({
        memberName: name,
        ...(pwd ? { memberPwd: pwd } : {}),
      });
      setMember(updated);
      setEditing(false);
      setPwd('');
      Swal.fire({
        title: '儲存成功',
        icon: 'success',
        timer: 1500,
        showConfirmButton: false,
        customClass: { popup: 'rounded-3xl' },
      });
    } catch {
      // 錯誤由 http.ts toast 處理
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setEditing(false);
    setName(member?.memberName ?? '');
    setPwd('');
  };

  return (
    <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-8 space-y-6 h-full flex flex-col">
      <div className="flex items-center justify-between border-b border-gray-100 pb-6">
        <h1 className="text-xl font-bold text-gray-900">個人資料</h1>
        {!editing && (
          <button
            onClick={() => setEditing(true)}
            className="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-xl transition-all"
          >
            <Pencil size={14} />
            編輯
          </button>
        )}
      </div>

      <div className="space-y-5">
        {/* 姓名 */}
        <div className="flex items-center gap-4">
          <span className="w-20 text-sm font-semibold text-gray-500 shrink-0">姓名</span>
          {editing ? (
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="flex-1 px-4 py-2 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={loading}
            />
          ) : (
            <span className="text-sm text-gray-900">{member?.memberName ?? '—'}</span>
          )}
        </div>

        {/* Email */}
        <div className="flex items-center gap-4">
          <span className="w-20 text-sm font-semibold text-gray-500 shrink-0">Email</span>
          <span className="text-sm text-gray-400">{member?.memberEmail ?? '—'}</span>
        </div>

        {/* 新密碼（編輯模式才顯示） */}
        {editing && (
          <div className="flex items-center gap-4">
            <span className="w-20 text-sm font-semibold text-gray-500 shrink-0">新密碼</span>
            <input
              type="password"
              value={pwd}
              onChange={(e) => setPwd(e.target.value)}
              placeholder="不修改請留空"
              className="flex-1 px-4 py-2 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={loading}
            />
          </div>
        )}

        {/* 加入日期 */}
        <div className="flex items-center gap-4">
          <span className="w-20 text-sm font-semibold text-gray-500 shrink-0">加入日期</span>
          <span className="text-sm text-gray-900">
            {member ? new Date(member.createdAt).toLocaleDateString('zh-TW') : '—'}
          </span>
        </div>
      </div>

      {/* 編輯操作按鈕 */}
      {editing && (
        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
          <button
            onClick={handleCancel}
            disabled={loading}
            className="flex items-center gap-2 px-5 py-2 text-sm font-semibold text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-xl transition-all disabled:opacity-50"
          >
            <X size={14} />
            取消
          </button>
          <button
            onClick={handleSave}
            disabled={loading}
            className="flex items-center gap-2 px-5 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-all disabled:opacity-50"
          >
            <Check size={14} />
            {loading ? '儲存中...' : '儲存'}
          </button>
        </div>
      )}
    </div>
  );
}
