<script>
import router from "@/router/index.js";

export default {
  setup() {
    const AddMenuItemPage = () => {
      router.push("/admin/voeg-toe-op-menu")
    }

    return {
      AddMenuItemPage
    }
  }
}
</script>

<template>
  <div class="admin-container">
    <h1 class="admin-title">Verander het formulier</h1>
    <button @click="AddMenuItemPage" class="primary-button">
      Voeg item toe op menu
    </button>
  </div>
</template>

<style scoped>
.admin-container {
  max-width: 1200px;
  margin: 1rem auto;
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.admin-title {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  font-weight: 600;
  padding-bottom: 0.5rem;
}

.primary-button {
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.primary-button:hover {
  background-color: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.primary-button:active {
  transform: translateY(0);
}
</style>
