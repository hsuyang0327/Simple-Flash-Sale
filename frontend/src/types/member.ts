export interface MemberAdminResponse {
  memberId: string;
  memberEmail: string;
  memberName: string;
  createdAt: string;
  updatedAt: string;
}

export interface MemberAdminPageResponse {
  content: MemberAdminResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
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