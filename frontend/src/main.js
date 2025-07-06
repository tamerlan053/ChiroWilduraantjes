window.global = window;
import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from "pinia"

// Import Font Awesome CSS
import '@fortawesome/fontawesome-free/css/all.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

export function formatToEuro(amount) {
    let formattedAmount = parseFloat(amount).toFixed(2);
    formattedAmount = formattedAmount.replace(".", ",");
    return `€${formattedAmount}`;
}

export function formatInput(input) {
    if (!input) return '';
    let formattedInput = input.trim();
    return formattedInput[0].toUpperCase() + formattedInput.slice(1).toLowerCase();
}

app.use(createPinia())
app.use(router)

app.mount("#app")

