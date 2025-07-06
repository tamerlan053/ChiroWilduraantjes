<template>
  <div class="container">
    <h2>Totaal aantal bestelde maaltijden</h2>
    <table aria-describedby="De totalen van al de bestellingen">
      <thead>
      <tr>
        <th>Maaltijd</th>
        <th>Totaal aantal</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(item, index) in totals" :key="index">
        <td>{{ formatInput(item.name) }}</td>
        <td>{{ item.quantity }}</td>
      </tr>
      </tbody>
    </table>
    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
  </div>
</template>


<script setup>
import { useStore } from "@/stores/index.js";
import { ref, onMounted } from "vue";
import {formatInput} from "../main.js";

const store = useStore();

const errorMessage = ref("");
const totals = ref([]);


onMounted(async () => {
  try {
    await store.fetchTotals();
    totals.value = store.totals;
  } catch (error) {
    errorMessage.value = "Failed to load totals";
    console.error("Error fetching totals:", error);
  }
});
</script>

<style scoped>
.container {
  width: 60%;
  height: 100%;
  padding-top: 0;
}

table {
  width: 100%;
  margin-top: 20px;
  border-collapse: collapse;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
  color: black;
}

th {
  background-color: #000080;
  color: white;
}

tbody tr:nth-child(odd) {
  background-color: #ebe5da;
}

tbody tr:nth-child(even) {
  background-color: white;
}

h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.error-message {
  color: red;
  font-weight: bold;
  margin-top: 20px;
}
</style>


