export interface MemberAdminResponse {
  memberId: string;
  memberEmail: string;
  memberName: string;
  createdAt: string;
  updatedAt: string;
}

export interface MemberAdminPageResponse {
  content: MemberAdminResponse[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface MemberClientResponse {
  memberId: string;
  memberEmail: string;
  memberName: string;
  createdAt: string;
  updatedAt: string;
}

export interface MemberUpdateRequest {
  memberName: string;
  memberPwd?: string;
}