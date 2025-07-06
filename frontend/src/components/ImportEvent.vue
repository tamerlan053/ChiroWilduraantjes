<script setup>
import { eventStore } from '@/stores/eventStore';
import {ref, onMounted, inject} from 'vue';

const eStore = eventStore();

const eventName = ref('');
const events = ref([]);

const fetchMenuItems = inject("fetchMenuItems");


onMounted(async () => {
  await eStore.getAllEvents();
  events.value = eStore.events; // this should now be an array of strings
});

const handleImport = async () => {
  if (eventName.value) {
    await eStore.importItemsFromPreviousEvent(eventName.value);
    if (fetchMenuItems) {
      await fetchMenuItems();
    } else {
      console.log("Fetch menu items doesn't work")
    }
  }
};
</script>

<template>
  <div>
    <h2> Importeer het menu van een vorig evenement</h2>
    <select v-model="eventName" class="input-field">
      <option disabled value="">Selecteer een vorig event</option>
      <option v-for="event in events" :key="event" :value="event">
        {{ event }}
      </option>
    </select>
    <button @click="handleImport" class="import-button">
      Importeer een vorig menu
    </button>
  </div>
</template>

<style scoped>
.input-field {
  padding: 8px;
  margin-bottom: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}


</style>
