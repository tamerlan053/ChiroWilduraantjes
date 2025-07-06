import { defineStore } from 'pinia'
import axios from 'axios'
const url = "http://localhost:8082/"

export const useStore = defineStore('store', {
    state: () => ({
        error: "",
        loading: false,
        totals: [],
    }),
    actions: {
        getAuthHeaders() {
            const token = localStorage.getItem("token");
            return token ? { headers: { Authorization: `Bearer ${token}` } } : {};
        },

        async getAdultsMenuItems() {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get(`${url}menu-items/adult`, this.getAuthHeaders());
                return response.data;
            } catch (err) {
                this.error = err;
            }
        },

        async getChildrenMenuItems() {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get(`${url}menu-items/child`, this.getAuthHeaders());
                return response.data.map(item => ({
                    ...item,
                    name: `Kinderportie ${item.name}`
                }));
            } catch (err) {
                this.error = err;
            }
        },

        async addItemOnMenu(item) {
            this.error = "";
            this.loading = true;
            try {
                await axios.post(`${url}menu-items/add`, item, this.getAuthHeaders());
                this.loading = false;
            } catch (err) {
                this.error = err.response?.data?.error || "Item kon niet worden toegevoegd";
                this.loading = false;
            }
        },

        async fetchTotals() {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get(`${url}orders/totals`, this.getAuthHeaders());
                this.totals = response.data;
                return this.totals;
            } catch (error) {
                this.error = error;
                return this.totals;
            }
        },

        async getAllOrders() {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get(`${url}orders`, this.getAuthHeaders());
                return response.data;
            } catch (error) {
                this.error = "Er is een fout opgetreden bij het ophalen van de bestellingen";
            } finally {
                this.loading = false;
            }
        },
        async getAllOrdersInProgress(){
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get( `${url}orders/inProgress`, this.getAuthHeaders());
                return response.data;
            }
            catch(error){
                this.error = "Er is een fout opgetreden bij het ophalen van de bestellingen"
            } finally {
                this.loading = false;
            }
        },

        async getOrderById(orderid) {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get(`${url}orders/${orderid}`, this.getAuthHeaders());
                return response.data;
            } catch (error) {
                this.error = "Er is een fout opgetreden bij het ophalen van de bestelling";
            } finally {
                this.loading = false;
            }
        },

        async updateOrder(orderId, editedMenuItems, editedDrinkTokenAmount) {
            this.error = "";
            this.loading = true;
            let body = { drinkTokens: editedDrinkTokenAmount.value, menuItems: editedMenuItems };
            try {
                await axios.put(`${url}orders/${orderId}`, body, this.getAuthHeaders());
                this.loading = false;
            } catch (error) {
                this.error = error.response?.data?.error || "Update mislukt";
                this.loading = false;
                console.log(error);
            }
        },

        async postOrder(orderData) {
            this.error = "";
            this.loading = true;
            try {
                await axios.post(`${url}orders`, orderData, this.getAuthHeaders());
                this.loading = false;
            } catch (err) {
                this.error = err.response?.data?.error || "Bestelling kon niet worden geplaatst";
                this.loading = false;
            }
        },

        async payOrder(orderId) {
            this.error = ""
            this.loading = true
            try {
                await axios.put(url + `orders/${orderId}/pay`, null, this.getAuthHeaders())

                this.loading = false
            } catch (err) {
                this.error = err.response?.data?.error || "Kon niet betalen"
                this.loading = false;
            }
        },
        async updateTableNumber(orderId, newTableNumber) {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.put(
                    `${url}orders/${orderId}/table`,
                    { tableNumber: newTableNumber },
                    this.getAuthHeaders()
                );
                this.loading = false;
                return response.data; // { geeft een message en tableNumber }
            } catch (error) {
                this.error = error.response?.data?.error || "Update mislukt";
                this.loading = false;
                console.log(error);
            }
        },
        async updateOrderInProgressStatus(orderId, orderInProgress){
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.put(
                    `${url}orders/${orderId}/setInProgress`,
                    { inProgress: orderInProgress},
                    this.getAuthHeaders()
                );
                this.loading = false;
                return response.data;
            } catch (error) {
                this.error = error.response?.data?.error || "Update mislukt"
                console.log(error)
            }
        },
        async updateReadyToServeStatus(orderId, readyToServe){
            this.error = "";
            this.loading = true;
            try {
                await axios.put(
                    `${url}orders/${orderId}/serveOrder`,
                    { readyToServe: readyToServe},
                    this.getAuthHeaders()
                )
                
            } catch (error) {
                this.error = error.response?.data?.error || "Update mislukt"
                console.log(error)
            }
        },
        async sendEmail(emailData) {
            this.error = "";
            this.loading = true;
            try {
                await axios.post(`${url}api/mail/send`, emailData, this.getAuthHeaders());
                this.loading = false;
            } catch (err) {
                this.error = err.response?.data?.error || "Mail niet verzonden"
                console.log(err);
            }
        }
    }
})

