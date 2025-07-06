<script setup>
import { ref } from 'vue';
import { eventStore } from "@/stores/eventStore.js";

const store = eventStore();
const newEventName = ref('');

const handleInputChange = async () => {
  if (newEventName.value.trim() !== '') {
    try {
      await store.changeEvent(newEventName.value);
      newEventName.value = '';
      window.location.reload();
    } catch (error) {
      console.error("Error adding event:", error);
    }
  }
};
</script>

<template>
  <div class="event-form-vertical">
    <input
        type="text"
        v-model="newEventName"
        @keyup.enter="handleInputChange"
        placeholder="Voeg een nieuw evenement toe"
        class="event-input"
    />
    <button @click="handleInputChange" class="submit-button">
      Voeg een evenement toe
    </button>
  </div>
</template>

<style scoped>
.event-form-vertical {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 300px;
  margin: 1rem 0;
}

.event-input {
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  width: 100%;
}

.event-input:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}

.event-input::placeholder {
  color: #94a3b8;
}

.submit-button {
  padding: 12px 16px;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.submit-button:hover {
  background-color: #2980b9;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.submit-button:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.submit-button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}
</style>