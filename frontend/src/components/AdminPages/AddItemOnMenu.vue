<script setup>
import {useStore} from "@/stores/index.js";
import {inject, ref} from "vue";

defineOptions({
  name: "AddItemOnMenu", // Add this line
});

const store = useStore();

const menuItem = ref({
  name: "",
  price: null,
  isChildFood: false,
});

const statusMessage = ref("");
const statusColor = ref("");
const isSubmitting = ref(false);

const fetchMenuItems = inject("fetchMenuItems");

const isItemAlreadyExist = async (name, isChildFood) => {
  const adultsMenuItems = await store.getAdultsMenuItems();
  const childrenMenuItems = await store.getChildrenMenuItems();

  const isOnAdultsMenu = adultsMenuItems.some(
      (item) =>
          item.name.trim().toLowerCase() === name.trim().toLowerCase() &&
          item.childFood === isChildFood
  );

  const isOnChildrenMenu = childrenMenuItems.some(
      (item) =>
          item.name.trim().toLowerCase() === `kinderportie ${name.trim().toLowerCase()}` &&
          item.childFood === isChildFood
  );

  return isOnAdultsMenu || isOnChildrenMenu;
};

const submitItem = async () => {
  try {
    isSubmitting.value = true;
    const itemExists = await isItemAlreadyExist(menuItem.value.name, menuItem.value.isChildFood);
    if (itemExists) {
      statusMessage.value = "Dit item staat al op het huidige menu!";
      statusColor.value = "red";
      showMessage();
      menuItem.value = {name: "", price: null, isChildFood: false};
      return;
    }

    if (menuItem.value.price <= 0) {
      statusMessage.value = "Prijs moet groter dan 0 zijn!";
      statusColor.value = "red";
      showMessage();
      return;
    }

    let requestBody = {
      name: menuItem.value.name.trim(),
      price: menuItem.value.price,
      isChildFood: menuItem.value.isChildFood,
    };

    await store.addItemOnMenu(requestBody);
    statusMessage.value = "Item succesvol toegevoegd!";
    statusColor.value = "green";
    showMessage();
    if (fetchMenuItems) {
      await fetchMenuItems();
    } else {
      console.log("Fetch menu items doesn't work")
    }// Call fetchMenuItems from PrototypeForm
    menuItem.value = {name: "", price: null, isChildFood: false};
  } catch (error) {
    console.error("Error submitting item:", error);
    statusMessage.value = "Er is iets mis gegaan met het toevoegen van dit item";
    statusColor.value = "red";
    showMessage();
  } finally {
    isSubmitting.value = false;
  }
};

const showMessage = () => {
  setTimeout(() => {
    statusMessage.value = "";
  }, 7500);
};
</script>

<template>
  <div>
    <form @submit.prevent="submitItem" class="form">
      <h2>Voeg item toe aan menu</h2>

      <div class="form-group">
        <label for="name">Naam item:</label>
        <input
            type="text"
            id="name"
            v-model="menuItem.name"
            required
            placeholder="Naam"
        />
      </div>

      <div class="form-group">
        <label for="price">Prijs item:</label>
        <input
            type="number"
            step="0.01"
            id="price"
            v-model="menuItem.price"
            required
            placeholder="Prijs"
        />
      </div>

      <div class="checkbox-group">
        <label for="isChildFood">Kinderportie?</label>
        <input type="checkbox" id="isChildFood" v-model="menuItem.isChildFood"/>
      </div>

      <div class="status-message-wrapper">
        <transition name="fade">
          <div v-if="statusMessage" :style="{color: statusColor}" class="status-message">
            {{ statusMessage }}
          </div>
        </transition>
      </div>

      <button type="submit" class="submit-btn" :disabled="isSubmitting">Toevoegen</button>
    </form>
  </div>
</template>

<style scoped>
h1 {
  background-color: #f9f9f9;
}

.checkbox-group {
  display: inline-flex;
  align-items: normal;
}

.checkbox-group label {
  white-space: nowrap;
}

.checkbox-group input[type="checkbox"] {
  margin-left: 35px;
}

.status-message-wrapper {
  height: 50px;
  width: auto;
  overflow: hidden;
}

.status-message {
  font-weight: bold;
  margin-bottom: 10px;
  font-size: 1em;
  text-align: center;
  line-height: 50px;
  transition: opacity 1s ease-out, color 1s ease-out;
}

.submit-btn {
  margin-top: 10px;
}
</style>
