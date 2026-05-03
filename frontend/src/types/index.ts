export interface Theme {
  id: string;
  name: string;
}

export interface JournalistProfileActivity {
  id: string;
  mediaName: string;
  role: string;
  specificEmail?: string;
  themes: Theme[];
}

export interface JournalistListItem {
  id: string;
  firstName: string;
  lastName: string;
  globalEmail?: string;
  mediaNames: string[];
}

export interface JournalistProfile {
  id: string;
  firstName: string;
  lastName: string;
  globalEmail?: string;
  globalPhone?: string;
  activities: JournalistProfileActivity[];
}

export interface JournalistCreate {
  firstName: string;
  lastName: string;
  globalEmail?: string;
  globalPhone?: string;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export type UserRole = 'ADMIN' | 'THEME_MANAGER' | 'USER';

export interface UserSummary {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: UserRole[];
}

export interface CurrentUser {
  username: string;
  firstName: string;
  lastName: string;
  roles: UserRole[];
}
