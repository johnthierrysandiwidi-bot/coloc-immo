import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import type { Role } from '@/types';

interface Props {
  roles?: Role[];
}

/**
 * Garde de route. Sans `roles`, exige seulement d'être authentifié.
 * Avec `roles`, exige au moins un des rôles.
 *
 * ⚠️ C'est une garde d'UX, pas de sécurité : l'autorisation réelle est
 * imposée par @PreAuthorize côté backend.
 */
export default function RoleGuard({ roles }: Props) {
  const { login, roles: mesRoles } = useAppSelector((s) => s.auth);
  const location = useLocation();

  if (!login) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  if (roles && !roles.some((r) => mesRoles.includes(r))) {
    return <Navigate to="/acces-refuse" replace />;
  }
  return <Outlet />;
}
