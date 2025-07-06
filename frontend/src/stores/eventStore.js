import {defineStore} from 'pinia'
import axios from 'axios'
const url = "http://localhost:8082/"


export const eventStore = defineStore('eventStore', {
    state: () => ({
        events: [],
        currentEvent: "",
        error: "",
        loading: false,
    }),
    actions: {
        getAuthHeaders() {
            const token = localStorage.getItem("token");
            return token ? {headers: {Authorization: `Bearer ${token}`}} : {};
        },

        async getAllEvents() {
            this.loading = true;
            this.error = "";
            try {
                const response = await axios.get(`${url}events`, this.getAuthHeaders());
                this.events = response.data;
            } catch (err) {
                this.error = err.response?.data || err.message;
            } finally {
                this.loading = false;
            }
        },

        async getCurrentEvent() {
            this.error = "";
            try {
                const response = await axios.get(`${url}events/current`, this.getAuthHeaders());
                this.currentEvent = response.data;
                return response.data;
            } catch (err) {
                this.error = err.response?.data || err.message;
            }
        },

        async changeEvent(newEventName) {
            this.error = "";
            this.loading = true;
            try {
                await axios.post(`${url}events`, newEventName, {
                    headers: {
                        'Content-Type': 'text/plain',
                        ...this.getAuthHeaders().headers,
                    },
                });
            } catch (err) {
                this.error = err.response?.data || err.message;
            } finally {
                this.loading = false;
            }
        },

        async deleteMenuItem(itemName, isChild) {
            this.error = "";
            try {
                await axios.delete(`${url}menu-items/delete`, {
                    data: {
                        itemName: itemName,
                        isChild: isChild,
                    },
                    headers: {
                        'Content-Type': 'application/json',
                        ...this.getAuthHeaders().headers,
                    },
                });
            } catch (error) {
                this.error = error.response?.data || error.message;
            }
        },

        async getEventChildrenMenuItems() {
            this.error = "";
            this.loading = true;
            try {
                const response = await axios.get(`${url}menu-items/child`, this.getAuthHeaders());
                return response.data.map(item => ({
                    ...item
                }));
            } catch (err) {
                this.error = err.response?.data || err.message;
            } finally {
                this.loading = false;
            }
        },

        async getTimeOptions() {
            this.error = "";
            try {
                const response = await axios.get(`${url}events/timeoptions`, this.getAuthHeaders());
                return response.data;
            } catch (err) {
                this.error = err.response?.data || err.message;
                return [];
            }
        },

        async addTimeOption(timeOption) {
            this.error = "";
            try {
                await axios.post(
                    `${url}events/add-time-option`,
                    timeOption,
                    {
                        headers: {
                            'Content-Type': 'text/plain',
                            ...this.getAuthHeaders().headers,
                        },
                    }
                );
                return true;
            } catch (err) {
                this.error = err.response?.data || err.message;
                return false;
            }
        },

        async deleteTimeOption(timeOption) {
            this.error = "";
            try {
                await axios.delete(`${url}events/delete`, {
                    data: timeOption,
                    headers: {
                        'Content-Type': 'text/plain',
                        ...this.getAuthHeaders().headers,
                    },
                });
                return true;
            } catch (err) {
                this.error = err.response?.data || err.message;
                return false;
            }
        },

        async importItemsFromPreviousEvent(eventName) {
            this.error = "";
            this.loading = true;
            try {
                await axios.post(
                    `${url}events/import`,
                    eventName,
                    {
                        headers: {
                            'Content-Type': 'text/plain',
                            ...this.getAuthHeaders().headers,
                        },
                    }
                );
            } catch (err) {
                this.error = err.response?.data || err.message;
                return false;
            } finally {
                this.loading = false;
            }
        }
    }
});
