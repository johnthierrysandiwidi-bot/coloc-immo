import { useDispatch, useSelector } from 'react-redux';
import type { TypedUseSelectorHook } from 'react-redux';
import type { AppDispatch, RootState } from './store';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

/** Raccourci : l'utilisateur possède-t-il au moins un de ces rôles ? */
export function useARole(...roles: string[]) {
  const mesRoles = useAppSelector((s) => s.auth.roles);
  return roles.some((r) => mesRoles.includes(r as never));
}
