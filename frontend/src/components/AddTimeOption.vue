<template>
  <div>
    <h2>Voeg een tijdstip toe</h2>
    <input v-model="newTimeOption" placeholder="Voorbeeld: zaterdag 18:00" class="time-input"/>
    <button @click="submitTimeOption" class="add-btn">Toevoegen</button>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>


<script>
import { eventStore } from "@/stores/eventStore";
import { inject, ref } from "vue";

export default {
  setup() {
    const store = eventStore();
    const newTimeOption = ref("");
    const fetchTimeOptions = inject("fetchTimeOptions");


    const submitTimeOption = async () => {
      if (!newTimeOption.value.trim()) {
        return;
      }

      const success = await store.addTimeOption(newTimeOption.value);
      if (success) {
        newTimeOption.value = "";
        if (fetchTimeOptions) {
          await fetchTimeOptions();
        } else {
          console.log("fetchMenuItems is not working");
        }
      } else {
        alert("Failed to add time option.");
      }
    };

    return { newTimeOption, submitTimeOption, error: store.error };
  },
};
</script>

<style scoped>
.time-input {
  padding: 10px;
  font-size: 1em;
  border-radius: 5px;
  border: 1px solid #ccc;
  width: 100%;  /* Optional: to make it full width */
  margin-bottom: 10px; /* Adds space below the input */
}

</style>