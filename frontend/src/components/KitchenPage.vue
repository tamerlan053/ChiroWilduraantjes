<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">Keuken bestellingen</h1>
    </div>

    <div class="orders-grid">
      <div v-for="order in activeOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <h3>Tafel {{ order.tableNumber }} ({{  formatInput(order.familyName) }})</h3>
          <div class="card-mussel-timer">
            <div class="mussel-timer-display">{{ order.musselTimerValue || '15:00' }}</div>
            <div class="mussel-timer-controls">
              <button
                  class="mussel-timer-btn"
                  @click="toggleOrderMusselTimer(order.id)"
                  :title="order.musselTimerRunning ? 'Pause' : 'Start'"
              >
                <i :class="order.musselTimerRunning ? 'fas fa-pause' : 'fas fa-play'"></i>
              </button>
              <button
                  class="mussel-timer-btn reset-btn"
                  @click="resetMusselTimer(order.id)"
                  title="Reset timer"
              >
                <i class="fas fa-redo-alt"></i>
              </button>
            </div>
          </div>
        </div>

        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="food-row">
            <span class="food-name">{{ formatInput(item.name) }}</span>
            <span class="food-quantity">
              <span class="completed">{{ item.completed }}</span>/<span class="ordered">{{ item.ordered }}</span>
            </span>
            <div class="controls">
              <button class="control-btn" @click="adjustQuantity(order.id, item.id, 1)">+</button>
              <button class="control-btn" @click="adjustQuantity(order.id, item.id, -1)">-</button>
            </div>
          </div>
        </div>

        <div class="arrival-timer-section">
          <i class="fas fa-clock"></i> Verstreken tijd: {{ order.arrivalTimerDisplayValue || '00:00:00' }}
        </div>

        <div class="special-instructions">
          <p>{{ order.specialInstructions }}</p>
        </div>

        <div v-if="orderIsComplete(order)" class="serve-button-container">
          <button class="serve-btn" @click="serveOrder(order.id)">
            <i class="fas fa-check-circle"></i> Serveren
          </button>
        </div>
      </div>

      <div v-if="activeOrders.length === 0" class="empty-state">
        <p>Geen actieve bestellingen op dit moment.</p>
      </div>
    </div>
  </div>
</template>

<script>
import { useStore } from "@/stores/index.js"
import { connectWebSocket } from "../utils/WebSocketService";
import Swal from "sweetalert2";
import {formatInput} from "../main.js";

export default {
  name: 'KitchenPage',
  data() {
    return {
      realOrders: [],
      orderMusselTimers: {},
      completedCounts: {},
      musselTimer: {},
      orderArrivalTimers: {}
    };
  },
  computed: {
    activeOrders() {
      this.realOrders.forEach(order => {
        if (!order.readyToServe && !this.orderArrivalTimers[order.id]?.intervalId) {

          this.startArrivalTimer(order);
        }
      });

      return this.realOrders
          .filter(order => !order.readyToServe)
          .sort((a, b) => new Date(a.arrivalTime) - new Date(b.arrivalTime))
          .map(order => {
            const items = order.menuItems.map(item => {
              const itemKey = `${order.id}_${item.name}`;
              if (this.completedCounts[itemKey] === undefined) {
                this.completedCounts[itemKey] = 0;
              }
              return {
                id: itemKey,
                name: item.name,
                ordered: item.quantity,
                completed: this.completedCounts[itemKey]
              };
            });

            if (!this.orderMusselTimers[order.id]) {
              this.orderMusselTimers[order.id] = {
                timerValue: "15:00",
                timerRunning: false,
                timerSeconds: 900
              };
            }

            if (!this.orderArrivalTimers[order.id]) {
              this.orderArrivalTimers[order.id] = {
                intervalId: null,
                displayValue: '00:00:00'
              };
            }

            return {
              id: order.id,
              tableNumber: order.tableNumber,
              familyName: order.familyName,
              arrivalTime: order.arrivalTime,
              items: items,
              specialInstructions: order.remarks || "Geen speciale instructies.",
              musselTimerValue: this.orderMusselTimers[order.id].timerValue,
              musselTimerRunning: this.orderMusselTimers[order.id].timerRunning,
              musselTimerSeconds: this.orderMusselTimers[order.id].timerSeconds,
              arrivalTimerDisplayValue: this.orderArrivalTimers[order.id]?.displayValue || '00:00:00'
            };
          });
    }
  },
  methods: {
    formatInput,
    formatTime(seconds) {
      const mins = Math.floor(Math.abs(seconds % 3600) / 60);
      const secs = Math.floor(Math.abs(seconds % 60));
      return [mins, secs]
          .map(num => num.toString().padStart(2, '0'))
          .join(':');
    },
    toggleOrderMusselTimer(orderId) {
      const musselTimerState = this.orderMusselTimers[orderId];
      if (!musselTimerState) return;

      musselTimerState.timerRunning = !musselTimerState.timerRunning;

      if (musselTimerState.timerRunning) {
        if (musselTimerState.timerSeconds <= 0) {
          musselTimerState.timerSeconds = 900;
          musselTimerState.timerValue = this.formatTime(musselTimerState.timerSeconds);
        }
        this.musselTimer[orderId] = setInterval(() => {
          if (musselTimerState.timerSeconds > 0) {
            musselTimerState.timerSeconds--;
            musselTimerState.timerValue = this.formatTime(musselTimerState.timerSeconds);
          } else {
            musselTimerState.timerRunning = false;
            this.stopMusselTimer(orderId);
          }
        }, 1000);
      } else {
        this.stopMusselTimer(orderId);
      }
    },
    stopMusselTimer(orderId) {
      if (this.musselTimer[orderId]) {
        clearInterval(this.musselTimer[orderId]);
        delete this.musselTimer[orderId];
      }
      if (this.orderMusselTimers[orderId]) {
        this.orderMusselTimers[orderId].timerRunning = false;
      }
    },
    resetMusselTimer(orderId) {
      const musselTimerState = this.orderMusselTimers[orderId];
      if (!musselTimerState) return;

      if (musselTimerState.timerRunning) {
        this.stopMusselTimer(orderId);
      }

      musselTimerState.timerSeconds = 900;
      musselTimerState.timerValue = this.formatTime(900);
    },
    orderIsComplete(order) {
      if (!order || !order.items || order.items.length === 0) return false;
      return order.items.every(item => item.completed >= item.ordered);
    },
    async serveOrder(orderId) {
      const result = await Swal.fire({
        title: "Bevestigen",
        text: "Dit gerecht serveren?",
        icon: "question",
        showCancelButton: true,
        confirmButtonText: "Serveer",
        cancelButtonText: "Annuleren"
      });

      if(result.isConfirmed){
        try {
          const store = useStore();
          console.log(`Order ${orderId} is ready to be served!`);
          this.stopMusselTimer(orderId);
          this.stopArrivalTimer(orderId);

          await store.updateReadyToServeStatus(orderId, true);
          const orders = await store.getAllOrdersInProgress();
          this.realOrders = orders;
          // Orders worden na 'serveren' uit de UI gehaald -> ?volgende stap?

        } catch (err) {
          console.error('Failed to serve order:', err);
          Swal.fire("Fout", err.response?.data?.error || 'Kon bestelling niet serveren', "error");
          // error.value = err.response?.data?.error || 'Failed to serve order'
        }
      }
    },
    adjustQuantity(orderId, itemId, increment) {
      const numericOrderId = parseInt(orderId);
      const order = this.realOrders.find(o => parseInt(o.id) === numericOrderId);
      if (!order) return;

      const itemKey = itemId;
      const itemName = itemKey.split('_')[1];
      if (!itemName) return;

      const menuItem = order.menuItems.find(item => item.name === itemName);
      if (!menuItem) return;

      if (this.completedCounts[itemKey] === undefined) {
        this.completedCounts[itemKey] = 0;
      }

      const currentCompleted = this.completedCounts[itemKey];
      const newCompleted = Math.min(
          menuItem.quantity,
          Math.max(0, currentCompleted + increment)
      );

      if (newCompleted !== currentCompleted) {
        this.completedCounts[itemKey] = newCompleted;
      }
    },
    handleInProgressUpdate(update) {
      console.log("Websocket received update: ", update);
      const store = useStore();
      store.getAllOrdersInProgress().then((orders) => {
        this.realOrders = orders;
        console.log("Orders updated from WebSocket:", this.realOrders);
      });
    },
    formatElapsedTime(ms) {
      if (ms < 0) ms = 0;
      const totalSeconds = Math.floor(ms / 1000);
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;

      return [hours, minutes, seconds]
          .map(num => num.toString().padStart(2, '0'))
          .join(':');
    },
    startArrivalTimer(order) {
      const orderId = order.id;
      if (this.orderArrivalTimers[orderId]?.intervalId) {
        return;
      }

      if (!this.orderArrivalTimers[orderId]) {
        this.orderArrivalTimers[orderId] = { intervalId: null, displayValue: '00:00:00' };
      }

      try {
        const arrivalTimestamp = new Date(order.arrivalTime).getTime();
        if (isNaN(arrivalTimestamp)) {
          console.error(`Invalid arrivalTime for order ${orderId}:`, order.arrivalTime);
          this.orderArrivalTimers[orderId].displayValue = 'Error';
          return;
        }

        const initialElapsedMs = Date.now() - arrivalTimestamp;
        this.orderArrivalTimers[orderId].displayValue = this.formatElapsedTime(initialElapsedMs);

        const intervalId = setInterval(() => {
          const elapsedMs = Date.now() - arrivalTimestamp;
          if (this.orderArrivalTimers[orderId]) {
            this.orderArrivalTimers[orderId].displayValue = this.formatElapsedTime(elapsedMs);
          } else {
            clearInterval(intervalId);
          }
        }, 1000);

        this.orderArrivalTimers[orderId].intervalId = intervalId;

      } catch (e) {
        console.error(`Error parsing arrivalTime or starting timer for order ${orderId}:`, e);
        this.orderArrivalTimers[orderId].displayValue = 'Error';
      }
    },
    stopArrivalTimer(orderId) {
      if (this.orderArrivalTimers[orderId]?.intervalId) {
        clearInterval(this.orderArrivalTimers[orderId].intervalId);
      }
      delete this.orderArrivalTimers[orderId];
    }
  },
  mounted() {
    const store = useStore();
    store.getAllOrdersInProgress().then((orders) => {
      this.realOrders = orders;
      console.log("Initial orders loaded:", orders);
    });

    connectWebSocket(this.handleInProgressUpdate);
  },
  beforeUnmount() {
    Object.keys(this.musselTimer).forEach(musselTimerId => {
      this.stopMusselTimer(musselTimerId);
    });
    Object.keys(this.orderArrivalTimers).forEach(arrivalTimerOrderId => {
      this.stopArrivalTimer(arrivalTimerOrderId);
    });
  },
  watch: {
    realOrders(newOrders, oldOrders) {
      const newOrderMap = new Map(newOrders.map(o => [o.id.toString(), o]));
      const oldOrderIds = new Set(oldOrders.map(o => o.id.toString()));
      const currentTimerOrderIds = new Set(Object.keys(this.orderArrivalTimers));

      newOrders.forEach(order => {
        const orderIdStr = order.id.toString();
        if (!oldOrderIds.has(orderIdStr) && !order.readyToServe) {
          if (!this.orderArrivalTimers[orderIdStr]) {
            this.orderArrivalTimers[orderIdStr] = { intervalId: null, displayValue: '00:00:00' };
          }
          this.startArrivalTimer(order);
        }
      });

      currentTimerOrderIds.forEach(orderIdStr => {
        const order = newOrderMap.get(orderIdStr);
        if (!order || order.readyToServe) {
          this.stopMusselTimer(orderIdStr);
          delete this.orderMusselTimers[orderIdStr];

          this.stopArrivalTimer(orderIdStr);

          Object.keys(this.completedCounts).forEach(key => {
            if (key.startsWith(`${orderIdStr}_`)) {
              delete this.completedCounts[key];
            }
          });
        }
      });
    }
  }
};
</script>

<style scoped>

.container {
  width: 95%;
  margin: auto;
  padding: 20px;
  background-color: white;
  min-height: 90vh;
  overflow-y: scroll;

}

.page-title {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
}

.container::-webkit-scrollbar {
  border-radius: 20px;
}

.container::-webkit-scrollbar-thumb {
  background-color: #000080;
  border-radius: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ccc;
}

.card-mussel-timer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mussel-timer-controls {
  display: flex;
  gap: 5px;
}

.mussel-timer-display {
  font-size: 20px;
  font-weight: bold;
  min-width: 50px;
}

.mussel-timer-btn {
  background-color: #ff9800;
  color: white;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.mussel-timer-btn:hover {
  background-color: #f57c00;
  transform: scale(1.05);
}

.mussel-timer-btn:active {
  transform: scale(0.95);
}

.reset-btn {
  background-color: #9e9e9e;
}

.reset-btn:hover {
  background-color: #757575;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(650px, 1fr));
  gap: 25px;
  margin-bottom: 20px;
  max-width: 1600px;
  margin-left: auto;
  margin-right: auto;
}

.order-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 3px 5px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 300px;
  min-width: 350px;
}

.order-header {
  background-color: #000080;
  color: white;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-header h3 {
  margin: 0;
  font-size: 20px;
}

.order-items {
  padding: 10px 15px;
  flex-grow: 1;
}

.food-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  gap: 10px;
}

.food-row:last-child {
  border-bottom: none;
}

.food-name {
  flex: 1;
  font-size: 18px;
  font-weight: 500;
  min-width: 150px;
}

.food-quantity {
  margin: 0 10px;
  font-weight: bold;
  color: #666;
  font-size: 16px;
  white-space: nowrap;
}

.completed {
  color: #4caf50;
}

.ordered {
  color: #ff9800;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
  text-align: center;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.empty-state p {
  font-size: 18px;
  color: #666;
}

.controls {
  display: flex;
  gap: 5px;
}

.control-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  color: white;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background-color 0.2s ease;
  flex-shrink: 0;
}

.control-btn:first-child {
  background-color: #4caf50;
}

.control-btn:first-child:hover {
  background-color: #3b9a3f;
}

.control-btn:first-child:active {
  background-color: #2d7d32;
}

.control-btn:last-child {
  background-color: #f44336;
}

.control-btn:last-child:hover {
  background-color: #e53935;
}

.control-btn:last-child:active {
  background-color: #c62828;
}

.special-instructions {
  background-color: #f9f9f9;
  padding: 10px 15px;
  border-top: 1px solid #eee;
}

.special-instructions p {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  color: #666;
}

.serve-button-container {
  margin-top: auto;
  padding: 15px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #eee;
  background-color: #f9f9f9;
}

.serve-btn {
  background-color: #4caf50;
  color: white;
  padding: 10px 25px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.serve-btn:hover {
  background-color: #3b9a3f;
  transform: scale(1.05);
}

.serve-btn:active {
  transform: scale(0.98);
  background-color: #2d7d32;
}

.serve-btn i {
  font-size: 18px;
}


.arrival-timer-section {
  padding: 8px 15px;
  border-top: 1px solid #eee;
  background-color: #f0f0f0;
  text-align: left;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.arrival-timer-section i {
  margin-right: 6px;
  color: #000080;
}



@media (max-width: 900px) {
  .container {
    width: 100%;
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .orders-grid {
    grid-template-columns: 1fr;
    max-width: 100%;
  }

  .order-items {
    padding: 5px 10px;
  }

  .food-name {
    font-size: 16px;
    min-width: 100px;
  }

  .card-mussel-timer {
    margin-top: 5px;
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .food-row {
    flex-wrap: wrap;
  }
}
</style>