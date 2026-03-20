export interface UserResponse {
  id: number;
  name: string;
  email: string;
  age: number;
  active: boolean;
}

export interface PagedUserResponse {
  content: UserResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
