<script setup>
import { useStore } from "@/stores/index.js";
import { eventStore } from "@/stores/eventStore.js";
import { ref, onMounted, computed } from "vue";
import { formatInput, formatToEuro } from "../main.js";

const store = useStore();
const thisEventStore = eventStore();

const form = ref({
  familyName: "",
  email: "",
  drinkTokens: null,
  remarks: "",
  selectedItems: {},
  arrivalTime: "",
  submitted: false,
});

const adultsMenuItems = ref([]);
const childrenMenuItems = ref([]);
const error = ref("");
const timeOptions = ref([]);

const showForm = ref(true);

const totalAmount = computed(() => {
  let total = 0;

  for (const [name, quantity] of Object.entries(form.value.selectedItems)) {
    if (quantity > 0) {
      const menuItem = [...adultsMenuItems.value, ...childrenMenuItems.value].find(i => i.name === name);
      if (menuItem) {
        total += menuItem.price * quantity;
      }
    }
  }

  const drinkTokenPrice = 2.0;
  const drinkTokens = Number(form.value.drinkTokens) || 0;

  total += drinkTokens * drinkTokenPrice;

  return total;
});

const fetchMenuItems = async () => {
  error.value = "";
  adultsMenuItems.value = [];
  childrenMenuItems.value = [];

  try {
    childrenMenuItems.value = await store.getChildrenMenuItems();
    adultsMenuItems.value = await store.getAdultsMenuItems();
  } catch (err) {
    error.value = err.response?.data?.error || "Failed to fetch menu items";
  }
};

const fetchTimeOptions = async () => {
  timeOptions.value = await thisEventStore.getTimeOptions();
};

const submitForm = async () => {
  const now = new Date();
  let dateStr = now.toISOString().split("T")[0];

  // Placeholders, functionaliteit om deze in te stellen moet nog toegevoegd worden
  const timeMap = {
    "zaterdag-18": "18:00:00",
    "zaterdag-20": "20:00:00",
    "zondag-18": "18:00:00",
  };

  let selectedTime = timeMap[form.value.arrivalTime] || "12:00:00";
  let localDateTime = `${dateStr}T${selectedTime}`;

  let menuItems = Object.entries(form.value.selectedItems)
      .filter(([_, quantity]) => quantity > 0)
      .map(([name, quantity]) => {
        let item = [...adultsMenuItems.value, ...childrenMenuItems.value].find(i => i.name === name);
        return {
          name,
          quantity,
          price: item ? item.price : 0,
        };
      });

  let requestBody = {
    email: form.value.email.trim(),
    arrivalTime: localDateTime,
    familyName: form.value.familyName.trim(),
    remarks: form.value.remarks.trim(),
    menuItems: menuItems,
    drinkTokens: form.value.drinkTokens,
  };

  try {
    sendMail(menuItems)
    await store.postOrder(requestBody);

    form.value.submitted = true;
    showForm.value = false;

    form.value = {
      familyName: "",
      email: form.value.email,
      drinkTokens: null,
      remarks: "",
      selectedItems: {},
      arrivalTime: "",
      submitted: true,
    };
  } catch (error) {
    console.error("Error submitting order:", error);
  }
};


const resetForm = () => {
  showForm.value = true; // Toon het formulier weer
  form.value.submitted = false;
  form.value.familyName = "";
  form.value.email = "";
  form.value.drinkTokens = null;
  form.value.remarks = "";
  form.value.selectedItems = {};
  form.value.arrivalTime = "";
};

const sendMail = (menuItems) => {
  const formattedItems = menuItems.map(item =>
      `- ${item.name}: ${item.quantity} × €${item.price.toFixed(2)}`
  ).join("\n");

  const drinkTokenCount = Number(form.value.drinkTokens) || 0;
  const drinkTokenLine = drinkTokenCount > 0
      ? `\n- Drankbonnetjes: ${drinkTokenCount} × €2.00`
      : "";

  const emailData = {
    to: form.value.email,
    subject: `Bestelling ${form.value.familyName}`,
    body: `Hallo Hallo!\n\nUw bestelling is goed aangekomen!\n\nAankomsttijd: ${form.value.arrivalTime}\nUw bestelling bevat:\n${formattedItems}${drinkTokenLine}\n\nTotaal: ${formatToEuro(totalAmount.value)}\n\nOpmerkingen: ${form.value.remarks || "Geen"}\n\nMet vragen? Twijfel niet om ons te contacteren!\n\nTot dan!`,
  };

  console.log(emailData);

  try {
    store.sendEmail(emailData);
  } catch (err) {
    console.error("Mail not send", err);
  }
}



// Laad de menu-items in bij het laden van de pagina
onMounted(fetchTimeOptions);
onMounted(fetchMenuItems);
</script>


<template>
  <div v-if="showForm && !form.submitted" class="container form-container">
    <h1>Inschrijvingsformulier</h1>

    <!-- Formulier voor bestelling, alleen zichtbaar als showForm waar is -->
    <form @submit.prevent="submitForm">
      <!-- Familie & Email in één rij -->
      <div class="form-row">
        <div class="form-group">
          <label for="familienaam">Familie</label>
          <input type="text" id="familienaam" v-model="form.familyName" required placeholder="Uw familienaam">
        </div>
        <div class="form-group">
          <label for="email">E-mail</label>
          <input type="email" id="email" v-model="form.email" required placeholder="Uw e-mail">
        </div>
      </div>

      <!-- Tijd selectie -->
      <div class="form-group">
        <label for="time">Wanneer komt u?</label>
        <select name="time" id="time" v-model="form.arrivalTime" required>
          <option value="" disabled selected>Selecteer</option>
          <option v-for="option in timeOptions" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>

      <label>Gerechten</label>
      <div class="section">
        <div class="menu-items">
          <h3>Normale porties</h3>
          <div v-for="item in adultsMenuItems" :key="item.id" class="menu-item">
            <label :for="item.id">{{ formatInput(item.name) }} - {{formatToEuro(item.price)}}</label>
            <input type="number" :id="item.id" v-model="form.selectedItems[item.name]" min="0">
          </div>
        </div>
        <!-- Kinderen Sectie -->
        <div class="menu-items">
          <h3>Kinderporties</h3>
          <div v-for="item in childrenMenuItems" :key="item.id" class="menu-item">
            <label :for="item.id">{{ formatInput(item.name) }} - {{formatToEuro(item.price)}}</label>
            <input type="number" :id="item.id" v-model="form.selectedItems[item.name]" min="0">
          </div>
        </div>
      </div>
      <!-- Volwassenen Sectie -->


      <!-- Drankbonnetjes -->
      <div class="form-row">
        <div class="form-group">
          <label for="drankbonnetjes">Drankbonnetjes</label>
          <input type="number" id="drankbonnetjes" v-model="form.drinkTokens" min="0">
        </div>
      </div>

      <!-- Opmerkingen -->
      <div class="form-group">
        <label for="comments">Opmerkingen</label>
        <textarea id="comments" placeholder="Voeg hier eventuele opmerkingen toe" v-model="form.remarks"></textarea>
      </div>

      <!-- Totaal & verzendknop -->
      <div class="sticky-footer">
        <div class="total-display">
          <strong>Huidig totaal:</strong> {{ formatToEuro(totalAmount) }}
        </div>
        <button type="submit" class="submit-btn">Verstuur</button>
      </div>
    </form>
  </div>
  <!-- Modal na succesvolle bestelling -->
  <div v-if="form.submitted" class="success-modal">
    <div class="container">
      <h2>Bestelling voltooid!</h2>
      <p>Bedankt voor uw bestelling! Er is een confirmatie mail gestuurd naar {{form.email}}</p>
      <p>Tot dan!</p>
      <button @click="resetForm">Terug naar het formulier</button>
    </div>
  </div>
</template>


<style>
.form-container {
  max-width: 800px;
  width: 100%;
  padding-top: 0;
  padding-bottom: 0;
  overflow-y: scroll;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: white;
  padding-top: 10px;
  padding-bottom: 5px;
}

h3 {
  font-size: 1.2em;
  margin-bottom: 10px;
}

.sticky-footer {
  position: sticky;
  bottom: 0;
  background: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
  flex-direction: column;
  gap: 10px;
  margin-left: -20px;
  margin-right: -20px;
}

.total-display {
  font-size: 1.2em;
  font-weight: bold;
  width: 100%;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group {
  flex: 1;
  margin-bottom: 15px;
}

label {
  display: block;
  font-size: 1.1em;
  margin-bottom: 5px;
}

input,
select,
textarea {
  width: 100%;
  padding: 10px;
  font-size: 1em;
  border-radius: 10px;
  border: 1px solid #ccc;
  box-sizing: border-box;
}

textarea {
  height: 100px;
  resize: vertical;
}

.section {
  border: 1px solid #ccc;
  padding: 0 15px 15px 15px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.menu-items input {
  width: 100%;
  margin-top: 5px;
}

.menu-items label {
  margin-top: 10px;
}

button.submit-btn {
  width: 100%;
  bottom: 0;
  z-index: 10;
}

.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 20px 35px;
  flex-direction: column;
  gap: 20px;
  text-align: center;
}

.success-modal h2 {
  font-size: 2em;
  color: black;
  margin-bottom: 10px;
}

.success-modal p {
  font-size: 1.2em;
  color: black;
  margin-bottom: 20px;
}

.success-modal button {
  width: auto;
  margin-top: 20px;
}

#comments {
  font-family: inherit;
}
</style>
