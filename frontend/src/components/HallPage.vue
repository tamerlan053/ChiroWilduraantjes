<template>
  <div class="container">
    <h1 class="page-title">Aangekomen klanten</h1>

    <!-- LIST VIEW -->
    <div v-if="currentView === 'list'">
      <div class="tabs">
        <button
            :class="['tab-button', { active: activeTab === 'new' }]"
            @click="activeTab = 'new'"
        >
          Nieuw aangekomen
        </button>
        <button
            :class="['tab-button', { active: activeTab === 'assigned' }]"
            @click="activeTab = 'assigned'"
        >
          Reeds aangekomen
        </button>
      </div>

      <div class="list-box">
        <!-- Tab: Nieuw aangekomen (orders zonder tafelnummer) -->
        <div v-if="activeTab === 'new'">
          <div
              class="order-box"
              v-for="order in ordersWithoutTable"
              :key="order.id"
          >
            <ul class="order-list">
              <li class="order-info">{{ formatInput(order.familyName) }}</li>
              <li>Aangekomen om {{ new Date(order.arrivalTime).toLocaleTimeString('nl-BE', { hour: '2-digit', minute: '2-digit' }) }}</li>
              <li>
                <button
                    @click="goToAssignTable(order)"
                    class="assign-btn"
                >
                  Tafel toewijzen
                </button>
              </li>
            </ul>
          </div>
        </div>

        <!-- Tab: Reeds aangekomen (orders met een tafelnummer) -->
        <div v-else-if="activeTab === 'assigned'">
          <button @click="toggleFilters" class="filter-toggle-btn">
            {{ showFilters ? 'Verberg filters' : 'Filter' }}
          </button>

          <div v-if="showFilters" class="filters">
            <input
                v-model="searchTable"
                type="text"
                placeholder="Zoek op tafelnummer"
                class="filter-input"
            />
            <input
                v-model="searchFamilyName"
                type="text"
                placeholder="Zoek op familienaam"
                class="filter-input"
            />
            <select v-model="sortOrder" class="filter-select">
              <option value="asc">Aankomst: Nieuwste eerst</option>
              <option value="desc">Aankomst: Oudste eerst</option>
            </select>
          </div>
          <div
              class="order-box"
              v-for="order in ordersWithTable"
              :key="order.id"
          >
            <ul class="order-list">
              <li class="order-info">{{ order.familyName.toUpperCase() }}</li>
              <li id="aangekomen">Aangekomen om {{ new Date(order.arrivalTime).toLocaleTimeString('nl-BE', { hour: '2-digit', minute: '2-digit' })}}</li>
              <li>
                <button
                    @click="goToAssignTable(order)"
                    class="modify-btn"
                >
                  Tafel {{ order.tableNumber }}
                </button>
              </li>
              <li>
                <button
                    @click="passToKitchen(order)"
                    class="kitchen-btn"
                >
                  Doorgeven aan keuken
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="currentView === 'assign'">
      <button @click="cancelAssignment" class="back-btn">←</button>
      <div class="assign-container">
        <h2 class="assign-title"> {{ isModifying ? `Tafel wijzigen van ${selectedOrder.familyName}`  : `Tafel toewijzen aan ${selectedOrder.familyName}` }}</h2>
        <input type="text" v-model="tableNumber" placeholder="Voer tafelnummer in" class="assign-input" />
        <div class="assign-button-group">
          <button :disabled="!tableNumber" @click="confirmTable" class="confirm-btn">
            Bevestigen
          </button>
          <button @click="cancelAssignment" class="cancel-btn">
            Annuleren
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useStore } from "@/stores/index.js";
import { formatInput } from "@/main.js";

export default {
  name: "HallPage",
  data() {
    return {
      currentView: "list",
      activeTab: "new",
      orders: [],
      selectedOrder: {},
      tableNumber: "",
      isModifying: false,
      searchTable: "",
      searchFamilyName: "",
      sortOrder: "asc",
      showFilters: false
    };
  },
  computed: {
    ordersWithoutTable() {
      return this.orders.filter((order) => order.payed && !order.tableNumber);
    },
    ordersWithTable() {
      return this.orders
          .filter((order) => order.tableNumber)
          .filter((order) =>
              order.tableNumber.toLowerCase().includes(this.searchTable.toLowerCase())
          )
          .filter((order) =>
              order.familyName.toLowerCase().includes(this.searchFamilyName.toLowerCase())
          )
          .sort((a, b) => {
            const t1 = new Date(a.arrivalTime).getTime();
            const t2 = new Date(b.arrivalTime).getTime();
            return this.sortOrder === "asc" ? t2 - t1 : t1 - t2;
          });
    }
  },
  methods: {
    formatInput,
    goToAssignTable(order) {
      this.selectedOrder = order;
      this.tableNumber = order.tableNumber || "";
      if(this.selectedOrder.tableNumber !== ""){
        this.isModifying = true;
      }
      this.currentView = "assign";
    },
    confirmTable() {
      const store = useStore();
      store
          .updateTableNumber(this.selectedOrder.id, this.tableNumber)
          .then(() => store.getAllOrders())
          .then((orders) => {
            this.orders = orders;
            this.currentView = "list";
          })
          .catch((err) => {
            console.error(err);
            this.currentView = "list";
          });
    },
    cancelAssignment() {
      this.currentView = "list";
    },
    passToKitchen(order) {
      console.log(`Order ${order.id} doorgeven aan keuken`);
      const store = useStore();
      store
          .updateOrderInProgressStatus(order.id, true)
          .then(() => store.getAllOrders())
          .then((orders) => {
            this.orders = orders;
            this.currentView = "list";
          })
    },
    toggleFilters() {
      this.showFilters = !this.showFilters;
      if (!this.showFilters) {
        this.searchTable = "";
        this.searchFamilyName = "";
        this.sortOrder = "asc";
      }
    }
  },
  mounted() {
    const store = useStore();
    store.getAllOrders().then((orders) => {
      this.orders = orders;
      console.log(this.orders);
    });
  }
};
</script>

<style scoped>

.container {
  width: 80%;
  margin: auto;
  padding: 20px;
  background-color: white;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 20px;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-button {
  flex: 1;
  padding: 10px;
  font-size: 18px;
  background-color: white;
  border: 1px solid lightgray;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  color: black;
}

.tab-button.active {
  background-color: #000080;
  color: white;
}

.list-box {
  min-height: 400px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.order-box {
  background-color: white;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.order-list {
  display: flex;
  justify-content: space-between;
  align-items: center;
  list-style: none;
  padding: 0;
  margin: 0;
}

.order-list li:last-child {
  margin-left: 10px;
}

.order-info {
  font-weight: bold;
  flex-grow: 1;
  margin-right: 10px;
}

.assign-btn,
.modify-btn,
.kitchen-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;

}

.assign-btn {
  background-color: #4caf50;
  color: white;

}

.assign-btn:hover {
  background-color: #45a049;
}

.modify-btn {
  background-color: #ff9800;
  color: white;
}

.modify-btn:hover {
  background-color: #f57c00;
}

.kitchen-btn {
  background-color: #f44336;
  color: white;
}

.kitchen-btn:hover {
  background-color: #d32f2f;
}

.back-btn {
  background: white;
  font-size: 32px;
  margin-top: -90px;
  z-index: 1000;
  width: fit-content;
  cursor: pointer;
  color: black;
  position: absolute;
}


.assign-container {
  margin-top: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.assign-title {
  font-size: 24px;
  margin-bottom: 15px;
  text-align: center;
}

.assign-input {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-sizing: border-box;
}

.assign-button-group {
  display: flex;
  justify-content: space-around;
  gap: 10px;
}

.confirm-btn,
.cancel-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.confirm-btn {
  background-color: #4caf50;
  color: white;
}

.confirm-btn:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.confirm-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.cancel-btn {
  background-color: #f44336;
  color: white;
}

.cancel-btn:hover {
  background-color: #d32f2f;
}

/* Voor Mobile */
@media (max-width: 768px) {
  .container {
    width: 95%;
    padding: 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .tabs {
    flex-direction: column;
  }

  .tab-button {
    font-size: 16px;
  }

  .list-box {
    height: auto;
    max-height: calc(60vh - 80px);
  }

  .order-list {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .order-list li:last-child {
  margin-left: 0;
}

  .order-info {
    margin-bottom: 5px;
    margin-right: 0;
  }

  .assign-btn,
  .modify-btn,
  .kitchen-btn {
    width: 100%;
    margin-left: 0;
    margin-bottom: 5px;
    text-align: center;
  }

  .assign-container {
    width: 90%;
    padding: 15px;
  }

  .assign-title {
    font-size: 20px;
  }

  .assign-button-group {
    flex-direction: column;
  }

  .confirm-btn,
  .cancel-btn {
    width: 100%;
  }
}
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.filter-input {
  padding: 8px;
  border-radius: 5px;
  border: 1px solid #ccc;
  flex: 1;
  min-width: 150px;
}

.filter-select {
  padding: 8px;
  border-radius: 5px;
  border: 1px solid #ccc;
}

.filter-toggle-btn {
  margin-bottom: 10px;
  padding: 8px 12px;
  background-color: #000080;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.filter-toggle-btn:hover {
  background-color: #1a1aa5;
}

#aangekomen {
  margin-right: 10px;
}

</style>
