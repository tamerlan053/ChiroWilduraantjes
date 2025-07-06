import SockJS from "sockjs-client";
import { Client } from '@stomp/stompjs'

let client = null;

export function connectWebSocket(onMessageReceived){
    client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8082/ws'),
        reconnectDelay: 5000,
        onConnect: () => {
            console.log("Connected to WebSocket");
            client.subscribe('/topic/orders/inProgress', message => {
                const body = JSON.parse(message.body);
                onMessageReceived(body);
            }  
        );  
        },
        onStompError: (frame) => {
            console.error = ("Broker reported error: " + frame.headers["message"]);
            console.error("Additional details: " + frame.body)
        }
    });
    client.activate();
}

export function disconnectWebSocket(){
    if (client){
        client.deactivate();
    }
}