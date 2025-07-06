<script>

import router from "@/router/index.js";

export default {
  setup() {
    const ChangeEventPage = () => {
      router.push("/admin/evenement")
    }

    return {
      ChangeEventPage
    }
  },
}

</script>

<template>
  <div class="container">
    <h1>Voeg een nieuw evenement toe</h1>
    <button @click="ChangeEventPage" class="nav-btn">Verander het evenement</button>
  </div>
</template>

<style scoped>
.container {
  padding-top: 0;
}
</style>