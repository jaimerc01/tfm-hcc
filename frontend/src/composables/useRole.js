import { computed } from 'vue'
import authService from '@/services/authService'
import { ROLES, hasRole } from '@/utils/roles'

// Deriva los roles del usuario actual a partir del JWT decodificado,
// centralizando la lógica que antes se duplicaba en cada componente/vista.
export function useRole() {
  const claims = computed(() => authService.getCurrentUser() || {})

  const isPaciente = computed(() => hasRole(claims.value, ROLES.PACIENTE))
  const isMedico = computed(() => hasRole(claims.value, ROLES.MEDICO))
  const isAdmin = computed(() => hasRole(claims.value, ROLES.ADMINISTRADOR))

  return { isPaciente, isMedico, isAdmin }
}
