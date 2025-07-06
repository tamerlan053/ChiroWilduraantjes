<script>
import { getRolesFromToken, hasRequiredRole, formatRoleName } from '@/utils/auth'

export default {
  data() {
    return {
      allRoles: ['ADMIN', 'KITCHEN', 'HALL', 'CASHIER'], // Hardcoded role list
      errorMessage: ''
    }
  },
  methods: {
    formatRoleName,
    selectRole(selectedRole) {
      if (this.hasAccess(selectedRole)) {
        this.$router.push({ name: selectedRole.toLowerCase() })
      } else {
        this.errorMessage = `Geen toegang tot ${formatRoleName(selectedRole)} rol`
      }
    },
    hasAccess(role) {
      return hasRequiredRole([role])
    }
  },
  computed: {
    userRoles() {
      return getRolesFromToken().map(role => role.replace('ROLE_', ''))
    }
  }
}
</script>

<template>
  <div class="role-page">
    <h1>Selecteer Rol</h1>
    <div class="role-grid">
      <button
          v-for="role in allRoles"
          :key="role"
          :class="{ 'disabled-role': !hasAccess(role) }"
          @click="selectRole(role)"
      >
        {{ formatRoleName(role) }}
        <span v-if="!hasAccess(role)" class="access-denied">(Geen toegang)</span>
      </button>
    </div>
    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
  </div>
</template>

<style scoped>
.role-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  max-width: 600px;
  margin: 2rem auto;
}

button {
  padding: 1.5rem;
  font-size: 1.1rem;
  border: 2px solid #2c3e50;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

button:hover:not(.disabled-role) {
  background-color: #2c3e50;
  color: white;
}

.disabled-role {
  opacity: 0.5;
  cursor: not-allowed;
  border-style: dashed;
}

.access-denied {
  display: block;
  font-size: 0.8rem;
  color: #e74c3c;
  margin-top: 0.5rem;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
}
</style>