<template>
  <div>
  </div>
  <div class="container form-container">
    <h1>Voorbeeldformulier: klik op een menu-item om deze te verwijderen</h1>
    <form>
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

      <div class="form-group">
        <label for="time">Wanneer komt u?</label>
        <ul class="time-options">
          <li v-for="option in timeOptions" :key="option" class="time-item" @click.prevent="deleteTimeOption(option)">
            {{ formatInput(option) }}
            <span class="delete-x" @click.prevent="deleteTimeOption(option)" > x </span>
          </li>
        </ul>
      </div>

      <label>Gerechten</label>
      <div class="section">
        <div class="menu-items">
          <h3>Normale porties</h3>
          <div v-for="item in adultsMenuItems" :key="item.id" class="menu-item">
            <div class="menu-label-wrapper">
              <label class="clickable-label" :for="item.id" @click.prevent="deleteMenuItem(item.name, false)">
                {{ formatInput(item.name) }} - {{ formatToEuro(item.price) }}
              </label>
              <span class="delete-x" @click.prevent="deleteMenuItem(item.name, false)">x</span>
            </div>
            <input type="number" :id="item.id" v-model="form.selectedItems[item.name]" min="0">
          </div>
        </div>

        <div class="menu-items">
          <h3>Kinderporties</h3>
          <div v-for="item in childrenMenuItems" :key="item.id" class="menu-item">
            <div class="menu-label-wrapper">
              <label class="clickable-label" :for="item.id" @click.prevent="deleteMenuItem(item.name, true)">
                {{ formatInput(`Kinderportie ${item.name}`) }} - {{ formatToEuro(item.price) }}
              </label>
              <span class="delete-x" @click.prevent="deleteMenuItem(item.name, true)">x</span>
            </div>
            <input type="number" :id="item.id" v-model="form.selectedItems[item.name]" min="0">
          </div>
        </div>
      </div>


      <div class="form-row">
        <div class="form-group">
          <label for="drankbonnetjes">Drankbonnetjes</label>
          <input type="number" id="drankbonnetjes" v-model="form.drinkTokens" min="0">
        </div>
      </div>

      <div class="form-group">
        <label for="comments">Opmerkingen</label>
        <textarea id="comments" placeholder="Voeg eventuele opmerkingen toe" v-model="form.remarks"></textarea>
      </div>
    </form>
  </div>
  <div class="admin-section">
    <h1>Verander het formulier</h1>
    <AddItemOnMenu/>
    <AddTimeOption/>
    <ImportEvent/>
  </div>
</template>

<script>
import { useStore } from "@/stores/index.js";
import { eventStore } from "@/stores/eventStore.js";
import { ref, onMounted, provide } from "vue";
import { formatInput, formatToEuro } from "@/main.js";
import AddItemOnMenu from "@/components/AdminPages/AddItemOnMenu.vue";
import AddTimeOption from "@/components/AddTimeOption.vue";
import ImportEvent from "@/components/ImportEvent.vue";

export default {
  components: {ImportEvent, AddTimeOption, AddItemOnMenu },
  methods: { formatToEuro, formatInput },
  name: "PrototypeForm",
  setup() {
    const store = useStore();
    const eStore = eventStore();
    const timeOptions = ref([]);

    const form = ref({
      familyName: "",
      email: "",
      drinkTokens: null,
      remarks: "",
      selectedItems: {},
      arrivalTime: "",
    });

    const adultsMenuItems = ref([]);
    const childrenMenuItems = ref([]);
    const error = ref("");

    const fetchMenuItems = async () => {
      error.value = "";
      adultsMenuItems.value = [];
      childrenMenuItems.value = [];

      try {
        childrenMenuItems.value = await eStore.getEventChildrenMenuItems();
        adultsMenuItems.value = await store.getAdultsMenuItems();
      } catch (err) {
        error.value = err.response?.data?.error || "Failed to fetch menu items";
      }
    };

    const fetchTimeOptions = async () => {
      try {
        timeOptions.value = await eStore.getTimeOptions();
      } catch (err) {
        console.error("Failed to fetch time options:", err);
      }
    };

    const deleteMenuItem = async (itemName, isChild) => {
      await eStore.deleteMenuItem(itemName, isChild);
      await fetchMenuItems();
    };

    const deleteTimeOption = async (option) => {
      const success = await eStore.deleteTimeOption(option);
      if (success) {
        await fetchTimeOptions();
      }
    };

    onMounted(fetchMenuItems);
    onMounted(fetchTimeOptions);

    provide("fetchMenuItems", fetchMenuItems);
    provide("fetchTimeOptions", fetchTimeOptions);

    return {
      store,
      adultsMenuItems,
      childrenMenuItems,
      error,
      form,
      timeOptions,
      deleteMenuItem,
      deleteTimeOption,
    };
  },
};
</script>

<style scoped>
.time-item p:hover {
  text-decoration: underline;
}

.clickable-label {
  cursor: pointer;
}

.clickable-label:hover {
  text-decoration: underline;
}


.menu-label-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.delete-x {
  color: red;
  font-weight: bold;
  margin-left: 10px;
  cursor: pointer;
  user-select: none;
  font-size: 20px;
}

.delete-x:hover {
  text-decoration: underline;
}

.admin-section {
  background-color: white; /* Or any color you like */
  border: 1px solid #ccc;
  padding: 20px;
  margin-top: 20px;
  border-radius: 10px;
}


.form-container {
  max-width: 1000px;
  width: 100%;
  padding-top: 0;
  padding-bottom: 15px;
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
  padding: 15px;
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

button {
  width: 100%;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.time-options {
  list-style: none;
  padding: 0;
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px solid #ddd;
  cursor: pointer;
  transition: background-color 0.2s;
}

.time-item:hover {
  text-decoration: underline;
}

</style>
