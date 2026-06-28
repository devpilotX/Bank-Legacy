import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Joins class names together and merges conflicting Tailwind classes so the last
// one wins. Every shadcn/ui component uses this.
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
