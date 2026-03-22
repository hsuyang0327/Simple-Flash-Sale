import http from '@/lib/http';

export const AuthService = {
  /**
   * 會員登入
   */
  login: async (email: string, password: string): Promise<void> => {
    await http.post('/client/auth/login', { email, password });
  },
  /**
   * 會員登出
   */
  logout: async (): Promise<void> => {
    await http.post('/client/auth/logout');
  },
  /**
   * 會員註冊
   */
  register: async (memberName: string, memberEmail: string, memberPwd: string): Promise<void> => {
    await http.post('/client/open/register', { memberName, memberEmail, memberPwd });
  },
};
