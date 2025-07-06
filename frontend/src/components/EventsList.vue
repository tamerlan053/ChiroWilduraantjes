<script setup>
import {ref, onMounted} from 'vue';
import {eventStore} from "@/stores/eventStore.js";

const store = eventStore();
const selectedEvent = ref(null);

const loadEvents = async () => {
  try {
    await store.getAllEvents();
    await store.getCurrentEvent();

    if (Array.isArray(store.events) && store.events.length > 0) {
      selectedEvent.value = store.currentEvent;
    }
  } catch (error) {
    console.error("Error loading events:", error);
  }
};

const handleEventClick = async (eventName) => {
  try {
    await store.changeEvent(eventName);
    selectedEvent.value = eventName;
    await loadEvents();
  } catch (error) {
    console.error("Error changing event:", error);
  }
};

onMounted(() => {
  loadEvents();
});
</script>

<template>
  <div class="events-container">
    <h2 class="events-title">Evenement management</h2>
    <ul class="events-list">
      <li
          v-for="event in store.events"
          :key="event"
          @click="handleEventClick(event)"
          :class="{
          'active-event': selectedEvent === event,
          'selected-event': store.currentEvent === event
        }"
          class="event-item"
      >
        {{ event }}
      </li>
    </ul>

    <p v-if="selectedEvent" class="selected-event">
      Huidig geselecteerd evenement: <strong>{{ selectedEvent }}</strong>
    </p>
  </div>
</template>

<style scoped>
.events-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 2rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.events-title {
  color: #2c3e50;
  margin-bottom: 1.5rem;
  font-weight: 600;
}

.events-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1.5rem 0;
}

.event-item {
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 4px solid transparent;
}

.event-item:hover {
  background-color: #f1f5f9;
  border-left: 4px solid #3498db;
  transform: translateX(4px);
}

.active-event {
  background-color: #e3f2fd;
  border-left: 4px solid #3498db;
  font-weight: 500;
  color: #2c3e50;
}

.selected-event {
  color: #7f8c8d;
  font-size: 0.9rem;
  padding: 0.5rem;
  background-color: #ecf0f1;
  border-radius: 4px;
  display: inline-block;
}
</style>
