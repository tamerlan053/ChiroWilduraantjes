<script setup>
import { ref, onMounted } from "vue";
import { useStore } from "@/stores/index.js";
import { useRoute } from "vue-router";
import {formatInput, formatToEuro} from "../main.js";
import Swal from "sweetalert2";
import router from "@/router/index.js"

const store = useStore();
const order = ref({
  familyName: "",
  remarks: "",
  menuItems: [],
  drinkTokens: 0,
  payed: false
});

const adultsMenuItems = ref([])
const childrenMenuItems = ref([])
const error = ref('')
let editMenuItemsList = ref([]);
let totalPrice = ref(0);
let changeOrder = ref(false)
const route = useRoute();
const drinkTokenList = [{name: "Enkele drankbon", price: 2, quantity: 1}, {name: "Drankkaart 5", price: 10, quantity: 5}, {name: "Drankkaart 10", price: 20, quantity: 10}, {name: "Drankkaart 20", price: 40, quantity: 20}];
let editedDrinkTokenAmount = ref(0);
let cash = ref(0)
let returnCash = ref(0)
let notEnoughMoney = ref(false)

function giveCashBack(){
  if (cash.value < totalPrice.value){
    notEnoughMoney.value = true
  }
  else{
    notEnoughMoney.value = false
    returnCash.value = cash.value - totalPrice.value
  }
}

function addDrinkToken(drinkTokenItem) {
  editedDrinkTokenAmount.value += drinkTokenItem.quantity;
  updateTotalPrice();
}

function subtractDrinkToken(drinkTokenItem) {
  if (editedDrinkTokenAmount.value >= drinkTokenItem.quantity) {
    editedDrinkTokenAmount.value -= drinkTokenItem.quantity;
  }
  updateTotalPrice();
}

function addMenuItem(menuItem) {
  let itemIndex = editMenuItemsList.value.findIndex(item => item.name === menuItem.name);

  if (itemIndex !== -1) {
    editMenuItemsList.value[itemIndex].quantity += 1;
  } else {
    menuItem = { ...menuItem, quantity: 1 };
    editMenuItemsList.value.push(menuItem);
  }

  updateTotalPrice();
}

function subtractMenuItem(menuItem) {

  let orderItemIndex = order.value.menuItems.findIndex(item => item.name === menuItem.name);
  let editItemIndex = editMenuItemsList.value.findIndex(item => item.name === menuItem.name);

  if (orderItemIndex !== -1) {
    if (editItemIndex !== -1) {
      let originalQuantity = order.value.menuItems[orderItemIndex].quantity;
          let quantityToSubtract = editMenuItemsList.value[editItemIndex].quantity;

          if (originalQuantity + quantityToSubtract > 0)
            editMenuItemsList.value[editItemIndex].quantity -= 1;
    } else {
      menuItem = { ...menuItem, quantity: -1 };
      editMenuItemsList.value.push(menuItem);
    }
  } else if (editItemIndex !== -1) {
    if (editMenuItemsList.value[editItemIndex].quantity > 0) {
      editMenuItemsList.value[editItemIndex].quantity -= 1;
    }
  }
  updateTotalPrice();
}

function findEditListQuantity(item) {
  let editItemIndex = editMenuItemsList.value.findIndex(menuItem => menuItem.name === item.name);

  if (editItemIndex === -1) {
    return 0;
  }

  return editMenuItemsList.value[editItemIndex].quantity;
}

function updateTotalPrice() {
  let total = 0;

  // Calculate the total for menu items from the original order
  for (const item of order.value.menuItems) {
    total += item.price * item.quantity;
  }

// Add the price of the edited menu items
  for (const item of editMenuItemsList.value) {
    total += item.price * item.quantity;
  }


  // Update totalPrice by adding the drink tokens (editedDrinkTokenAmount * 2)
  totalPrice.value = total + (editedDrinkTokenAmount.value * 2);
}


const applyEdit = async (orderMenuItemsList, editMenuItemsList) => {
  try {
    //maakt copy van het order menu
    let updatedOrder = [...orderMenuItemsList];

    //voegt de edits to aan updatedOrder
    for (let editItem of editMenuItemsList) {
      let itemOrderIndex = updatedOrder.findIndex(menuItem => menuItem.name === editItem.name)

      if (itemOrderIndex !== -1) {
        //als de item bestaat in de originele order update de quantity
        updatedOrder[itemOrderIndex].quantity += editItem.quantity;

        //als quantity 0 of minder is verwijder het van de order
        if (updatedOrder[itemOrderIndex].quantity <= 0) {
          updatedOrder.splice(itemOrderIndex, 1);
        }
      } else {
        //als het niet bestaat in het originele order voeg het toe
        if (editItem.quantity > 0) {
          updatedOrder.push({ ...editItem });
        }
      }
    }

    //maak api call
    await store.updateOrder(route.params.id, updatedOrder, editedDrinkTokenAmount);

    //resets de lijst en zet de knop terug op aanpassen
    editMenuItemsList.value = [];
    changeOrder.value = false;

    //herlaad de pagina
    location.reload()
  } catch (err) {
    error.value = err.response?.data?.error || 'Failed to update order';
  }
};

const pay = async () => {
  const orderId = route.params.id

  const result = await Swal.fire({
    title: "Bevestigen",
    text: "Is alles in orde?",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Ja",
    cancelButtonText: "Annuleren",
  })

  if (result.isConfirmed){
    try{
      await store.payOrder(orderId)
      order.value.payed = true;
      await router.push({ name: 'cashier'})
    } catch (err) {
      error.value = err.response?.data?.error || 'Failed'
    }
  }
}

onMounted(async () => {
  const orderId = route.params.id;

  try {
    childrenMenuItems.value = await store.getChildrenMenuItems();
    adultsMenuItems.value = await store.getAdultsMenuItems();
    order.value = await store.getOrderById(orderId);
    editedDrinkTokenAmount.value = parseInt(order.value.drinkTokens);
    console.log(order.value)
  } catch (err){
    error.value = err.response.data.error || 'Failed to fetch menu items'
  }

  updateTotalPrice();
});
</script>

<template>
  <div class="main-container">

    <!-- Left container with order details -->
    <div class="container left-container">
      <h1 class="family-name"> Bestelling - {{ formatInput(order.familyName.trim()) }} </h1>
      <table class="menu-table" aria-describedby="Tabel van de bestelling">
        <thead >
          <tr>
            <th> Aantal</th>
            <th> Menu-item</th>
            <th> Prijs</th>
          </tr>
        </thead>
        <tbody>
        <tr v-for="item in order.menuItems" :key="item.name">
          <td v-if="(item.quantity || 0) + (findEditListQuantity(item) || 0) > 0">{{ item.quantity + findEditListQuantity(item) }}x</td>
          <td v-if="(item.quantity || 0) + (findEditListQuantity(item) || 0) > 0">{{ formatInput(item.name) }}</td>
          <td v-if="(item.quantity || 0) + (findEditListQuantity(item) || 0) > 0">{{ formatToEuro( item.price * (item.quantity + findEditListQuantity(item))) }}</td>
        </tr>

        <tr v-for="(item, index) in editMenuItemsList" :key="index">
          <td v-if="order.menuItems.findIndex(menuItem => menuItem.name === item.name) === -1 && item.quantity !== 0">{{ item.quantity }}x</td>
          <td v-if="order.menuItems.findIndex(menuItem => menuItem.name === item.name) === -1 && item.quantity !== 0">{{ formatInput(item.name) }}</td>
          <td v-if="order.menuItems.findIndex(menuItem => menuItem.name === item.name) === -1 && item.quantity !== 0">{{ formatToEuro(item.price * item.quantity) }}</td>
        </tr>

        <tr v-if="editedDrinkTokenAmount !== 0">
          <td>{{editedDrinkTokenAmount }}x</td>
          <td>Drankbonnen</td>
          <td>{{ formatToEuro(editedDrinkTokenAmount * 2) }}</td>
        </tr>

        </tbody>

        <tfoot>
        <tr>
          <td><strong>Totaal</strong></td>
          <td></td>
          <td><strong>{{ formatToEuro(totalPrice) }}</strong></td>
        </tr>
        </tfoot>
      </table>

      <div class="remarks-container">
        <h2>Opmerkingen</h2>
        <div class="remarks-wrapper">
          <p class="remarks"> {{ order.remarks }}</p>
        </div>
      </div>

      <div class="button-container">
        <button @click="changeOrder ? applyEdit(order.menuItems, editMenuItemsList) : (changeOrder = !changeOrder)">
          {{ changeOrder ? "Bevestigen" : "Aanpassen" }}
        </button>
      </div>
    </div>

    <!-- Right container for total price or any other content -->
    <div class="container right-container">
      <div v-if="!changeOrder">

        <div class="payment-container">
          <h1 class="payment-title">Bedrag nu te betalen</h1>
          <h2 class="amount">{{ formatToEuro(totalPrice) }}</h2>

          <div class="input-container">
            <label for="cash-input">Contant ontvangen:</label>
            <input
                type="number"
                id="cash-input"
                v-model="cash"
                class="cash-input"
                @input="giveCashBack"
            />

            <div v-if="!notEnoughMoney">
              <h1>Wisselgeld: {{ formatToEuro(returnCash) }}</h1>
            </div>

            <div v-else>
              <h1>Onvoldoende cash afgegeven.</h1>
            </div>

            <button @click="pay" class="pay-button">Afrekenen</button>
          </div>
        </div>
      </div>

      <!-- Edit page -->
      <div v-else>
        <!-- Adult menu items -->
        <div>
          <h1>Gerechten</h1>
          <div class="menu-section">
            <table class="menu-items-table" aria-describedby="Volwassenenopties">
              <thead>
                <tr>
                  <th></th>
                </tr>
              </thead>
              <tbody>
              <tr v-for="(menuItem, index) in adultsMenuItems" :key="index" :class="{ 'alternate-row': index % 2 === 0 }">
                <td class="button-cell">
                  <button class="control-button" @click="subtractMenuItem(menuItem)">-</button>
                </td>
                <td class="item-name">{{ formatInput(menuItem.name) }}</td>
                <td class="item-price">{{ formatToEuro(menuItem.price) }}</td>
                <td class="button-cell">
                  <button class="control-button" @click="addMenuItem(menuItem)">+</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Children menu items -->
          <div class="menu-section">
            <table class="menu-items-table" aria-describedby="Kinderopties">
              <thead>
              <tr>
                <th></th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(menuItem, index) in childrenMenuItems" :key="index" :class="{ 'alternate-row': index % 2 === 0 }">
                <td class="button-cell">
                  <button class="control-button" @click="subtractMenuItem(menuItem, true)">-</button>
                </td>
                <td class="item-name">{{ formatInput(menuItem.name) }}</td>
                <td class="item-price">{{ formatToEuro(menuItem.price) }}</td>
                <td class="button-cell">
                  <button class="control-button" @click="addMenuItem(menuItem, true)">+</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Drinktokens -->
          <h1>Drankbonnetjes</h1>
          <div class="menu-section">
            <table class="menu-items-table" aria-describedby="Drankopties">
              <thead>
              <tr>
                <th> </th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(drinkTokenItem, index) in drinkTokenList" :key="index" :class="{ 'alternate-row': index % 2 === 0 }">
                <td class="button-cell">
                  <button class="control-button" @click="subtractDrinkToken(drinkTokenItem)">-</button>
                </td>
                <td class="item-name">{{ drinkTokenItem.name }}</td>
                <td class="item-price">{{ formatToEuro(drinkTokenItem.price) }}</td>
                <td class="button-cell">
                  <button class="control-button" @click="addDrinkToken(drinkTokenItem)">+</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
      </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

.payment-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

.payment-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 15px;
}

.amount {
  font-size: 2.8rem;
  font-weight: bold;
  color: #b00000;
  margin-bottom: 25px;
}

.input-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  width: 100%;
}

.cash-input {
  border: 2px solid #ccc;
  border-radius: 5px;
  padding: 14px;
  width: 180px;
  font-size: 1.3rem;
  text-align: center;
}

.pay-button {
  background-color: #000080;
  color: white;
  padding: 14px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.3rem;
  width: 100%;
  margin-bottom: 25px;
}

.pay-button:hover {
  background-color: #0d0d54;
}

.sumup-button {
  background-color: #000080;
  color: white;
  width: 100%;
  padding: 18px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.3rem;
}

.sumup-button:hover {
  background-color: #0d0d54;
}

.main-container {
  display: flex;
  height: 85vh;
  width: 100vw; /* Full width */
  box-sizing: border-box;
  top: 0;
}

.left-container, .right-container {
  flex: 1; /* Make both containers take up equal space */
  min-height: 100%; /* Ensure they stretch */
  overflow-y: auto; /* Allow scrolling if content exceeds container height */
}

.left-container {
  background: #e5d4af;
  padding: 20px 10px 10px 10px;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.right-container {
  background: white;
  padding: 0 20px;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.remarks-container {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.remarks-wrapper {
  width: 100%;
  overflow-wrap: break-word;
  word-wrap: break-word;
  hyphens: auto;
  max-height: 30vh;
  overflow-y: auto;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  padding: 8px;
}

.button-container {
  margin-top: 10px;
  padding: 10px 0;
}

.button-container button {
  padding: 10px 15px;
  background-color: #000080;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.button-container button:hover {
  background-color: #0d0d54;
}

.family-name {
  margin: 0;
  padding: 10px 0;
  font-size: 2.5rem;
  font-weight: bold;
  display: flex;
  flex-direction: column;
  align-items: center; /* Align text to the top */
  justify-content: flex-start; /* Push content to the top */
  border-radius: 4px;
  background-color: rgb(255,255,255,0.25);
}

.menu-table {
  width: 100%;
  border-collapse: collapse;
}

.menu-table th,
.menu-table td,
.menu-table tfoot {
  border-top: 3px solid #c4b697;
  padding: 20px;
  text-align: left;
  font-size: 1.5rem;
  width: auto;
}

.menu-table th {
  font-weight: bold;
}

.remarks {
  font-size: 1.2rem;
  margin: 0;
  padding: 0;
  white-space: pre-wrap; /* Preserves whitespace and allows wrapping */
}

.menu-section {
  margin-bottom: 20px;
}

.menu-items-table {
  width: 100%;
  border-collapse: collapse;
}

.menu-items-table tr {
  height: 50px;
}

.alternate-row {
  background-color: #f2f2f2;
}

.button-cell {
  width: 40px;
  text-align: center;
}

.control-button {
  width: 30px;
  height: 30px;
  font-size: 1.2rem;
  font-weight: bold;
  background-color: #000080;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.control-button:hover {
  background-color: #0d0d54;
}

.item-name {
  padding-left: 10px;
  font-size: 1.1rem;
  flex-grow: 1;
}

.item-price {
  text-align: right;
  padding-right: 10px;
  font-size: 1.1rem;
  width: 80px;
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .main-container {
    flex-direction: column;
    height: auto;
  }

  .left-container, .right-container {
    width: 100%;
    min-height: 50vh;
  }

  .menu-table th,
  .menu-table td,
  .menu-table tfoot {
    font-size: 1.2rem;
    padding: 10px;
  }

  .family-name {
    font-size: 2rem;
  }

  .remarks-wrapper {
    max-height: 20vh;
  }
}
</style>