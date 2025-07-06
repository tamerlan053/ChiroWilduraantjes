<script setup>
import { ref, computed, onMounted } from "vue";
import { useStore } from "@/stores/index.js";
import router from "@/router/index.js";

const store = useStore();
const searchQuery = ref("");
const orders = ref([]);

const goToOrder = (orderid) => {
  router.push(`/kassa/${orderid}`)
}

const filteredOrders = computed(() =>
    orders.value.filter((order) =>
        order.payed === false &&
        order.familyName.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
);

onMounted(async () => {
  orders.value = await store.getAllOrders();
  console.log(orders.value)
});

</script>

<template>
  <div class="container">
    <h1 class="page-title">Overzicht van de bestellingen</h1>
    <div class="search-bar-box">
      <input
          type="text"
          v-model="searchQuery"
          placeholder="Geef de familienaam in"
          class="search-bar"
      />
    </div>
    <div class="list-box">
      <div class="order-box" v-for="order in filteredOrders" :key="order.id" :order="order" @click="goToOrder(order.id)">
        <ul class="order-list">
          <li id="id">{{ order.id }}</li>
          <li>{{ order.familyName.toUpperCase()}}</li>
          <li></li>
        </ul>
      </div>
    </div>
  </div>

</template>

<style scoped>
.container {
  width: 80%;
  overflow: hidden;
  padding: 20px;
}

h1 {
  margin: 0;
  padding: 0;
}

.list-box {
  max-height: 70vh;
  margin-top: 5px;
  overflow-y: scroll;
  overflow-x: hidden;
}

.search-bar-box {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: white;
  padding-top: 5px;
  padding-left: 10px;
  padding-right: 10px;
  margin-top: 0;
}

.search-bar {
  width: 100%;
  max-width: 500px;
  padding: 10px;
  border: 2px solid lightgray;
  border-radius: 10px;
  margin: 5px 0;
  font-size: 18px;
}

.order-box {
  background: white;
  padding: 10px 5px;
  border-radius: 10px;
  margin: 5px;
  width: 98%;
  box-sizing: border-box;
  border: 1px solid lightgray;
  cursor: pointer;
}

.order-box:hover {
  background: lightgray;
}

.order-list {
  list-style: none;
  justify-content: space-between;
  display: flex;
}

.order-list li {
  font-size: 24px;
  color: black;
}
</style>