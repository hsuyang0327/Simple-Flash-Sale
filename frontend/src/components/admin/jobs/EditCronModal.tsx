'use client';

import { X, Info, CheckCircle2, AlertCircle } from 'lucide-react';
import { useState, useEffect } from 'react';
// 使用此路徑可避免 Next.js 找不到語系檔的問題
import cronstrue from 'cronstrue/dist/cronstrue-i18n';

interface EditCronModalProps {
  isOpen: boolean;
  onClose: () => void;
  jobName: string;
  currentCron: string;
  onSave?: (newCron: string) => void;
}

export default function EditCronModal({ 
  isOpen, 
  onClose, 
  jobName, 
  currentCron, 
  onSave 
}: EditCronModalProps) {
  const [inputValue, setInputValue] = useState(currentCron);
  const [description, setDescription] = useState("");
  const [isError, setIsError] = useState(false);

  // 初始化資料
  useEffect(() => {
    if (isOpen) {
      setInputValue(currentCron);
    }
  }, [currentCron, isOpen]);

  // 即時中文解析邏輯
  useEffect(() => {
    if (!inputValue || inputValue.trim() === "") {
      setDescription("請輸入 Cron 表達式");
      setIsError(true);
      return;
    }

    try {
      // locale 務必設定為 zh_Hant
      const translated = cronstrue.toString(inputValue, { 
        locale: "zh_TW", 
        use24HourTimeFormat: true 
      });
      setDescription(translated);
      setIsError(false);
    } catch (err) {
      setDescription("無效的格式，請檢查空格或符號是否正確");
      setIsError(true);
    }
  }, [inputValue]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      {/* 遮罩 */}
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={onClose} />
      
      {/* 內容主體 */}
      <div className="relative bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl p-10 space-y-8 animate-in zoom-in-95 duration-300">
        
        {/* Header */}
        <div className="flex justify-between items-start border-b border-gray-50 pb-6">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <div className="w-1.5 h-6 bg-blue-600 rounded-full"></div>
              <h3 className="text-xl font-bold text-gray-900 tracking-tight">修改執行策略</h3>
            </div>
            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-4">{jobName}</p>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-slate-100 rounded-full text-gray-400">
            <X size={20} />
          </button>
        </div>

        {/* Input Area */}
        <div className="space-y-6">
          <div>
            <div className="flex justify-between mb-2 px-1 text-[10px] font-black uppercase tracking-widest text-gray-400">
              <span>Cron 表達式</span>
              <span className="font-mono text-blue-500/40">秒 分 時 日 月 週</span>
            </div>
            <input 
              type="text" 
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              className={`w-full bg-slate-50 border-2 rounded-2xl px-6 py-5 font-mono text-lg font-bold tracking-widest transition-all focus:outline-none focus:ring-8 ${
                isError 
                  ? 'border-rose-100 focus:border-rose-500 focus:ring-rose-500/5 text-rose-600' 
                  : 'border-slate-100 focus:border-blue-500 focus:ring-blue-500/5 text-gray-900 shadow-inner'
              }`}
            />
          </div>

          {/* 解析結果區塊 */}
          <div className={`rounded-2xl p-5 flex gap-4 border transition-all ${
            isError ? 'bg-rose-50/50 border-rose-100' : 'bg-blue-50/50 border-blue-100 shadow-sm'
          }`}>
            <div className={`w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm shrink-0 ${
              isError ? 'text-rose-500' : 'text-blue-600'
            }`}>
              {isError ? <AlertCircle size={18} /> : <Info size={18} />}
            </div>
            <div className="flex flex-col justify-center">
              <p className={`text-[10px] font-black uppercase tracking-widest mb-1 ${
                isError ? 'text-rose-400' : 'text-blue-400'
              }`}>
                {isError ? '格式錯誤' : '排程解讀 (中文)'}
              </p>
              <p className={`text-sm font-bold leading-snug ${
                isError ? 'text-rose-900' : 'text-blue-900'
              }`}>
                {description}
              </p>
            </div>
          </div>
        </div>

        {/* 按鈕區域 */}
        <div className="flex gap-3 pt-2">
          <button onClick={onClose} className="flex-1 px-6 py-5 rounded-2xl text-xs font-black text-gray-500 hover:bg-slate-50 transition-all tracking-widest">
            取消返回
          </button>
          <button 
            disabled={isError}
            onClick={() => { if (onSave) onSave(inputValue); onClose(); }}
            className="flex-1 px-6 py-5 bg-gray-900 rounded-2xl text-xs font-black text-white shadow-xl hover:bg-blue-600 disabled:opacity-20 disabled:cursor-not-allowed transition-all active:scale-95 flex items-center justify-center gap-2 tracking-widest"
          >
            <CheckCircle2 size={16} />確認更新
          </button>
        </div>
      </div>
    </div>
  );
}