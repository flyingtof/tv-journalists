export interface Theme {
  id: string;
  name: string;
}

export interface Activity {
  id: string;
  mediaId: string;
  mediaName: string;
  role: string;
  specificEmail?: string;
  specificPhone?: string;
  themes: Theme[];
}

export interface Journalist {
  id: string;
  firstName: string;
  lastName: string;
  globalEmail?: string;
  globalPhone?: string;
  activities: Activity[];
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
