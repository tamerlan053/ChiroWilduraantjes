<template>
  <div class="login-container">
    <h2>Login</h2>
    <form @submit.prevent="login">
      <div>
        <label for="username">Gebruikersnaam:</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div>
        <label for="password">Wachtwoord:</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit">Login</button>
      <p v-if="errorMessage" class="error">{{errorMessage}}</p>
    </form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      username: "",
      password: "",
      errorMessage: ""
    };
  },
  methods: {
    async login() {
      //Hier halen we de JWT op
      try {
        const response = await axios.post('http://localhost:8082/api/users/login',
            {
              username: this.username,
              password: this.password
            })

        const token = response.data;
        console.log(token);
        localStorage.setItem('token', token);
        this.$router.push({
          name: 'Roles',
          params: { username: this.username }
        })

      }
      catch (error){
        this.errorMessage = "Invalid username or password"
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 300px;
  margin: auto;
  text-align: center;
}

button {
  margin-top: 15px;
}

.error {
  color: red;
}
</style>