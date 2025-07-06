package be.pxl.research.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketConfigTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    void contextLoads() {
        assertNotNull(webSocketConfig);
    }

    @Test
    void testWebSocketConnection() throws InterruptedException, ExecutionException, TimeoutException {
        WebSocketStompClient stompClient = createWebSocketClient();
        StompSession session = connectToWebSocket(stompClient);
        assertTrue(session.isConnected());
        session.disconnect();
    }

    private WebSocketStompClient createWebSocketClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new StringMessageConverter());
        return stompClient;
    }

    private StompSession connectToWebSocket(WebSocketStompClient stompClient)
            throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();

        stompClient.connectAsync(
                "ws://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        sessionFuture.complete(session);
                    }
                }
        );

        return sessionFuture.get(5, TimeUnit.SECONDS);
    }
}
