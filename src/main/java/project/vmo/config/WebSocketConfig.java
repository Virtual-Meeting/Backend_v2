package project.vmo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import project.vmo.SignalHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalHandler signalHandler;

    @Autowired
    public WebSocketConfig(SignalHandler signalHandler) {
        this.signalHandler = signalHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalHandler, "")
                .setAllowedOrigins("https://localhost:3000", "http://localhost:3000", "https://vtestmo.kro.kr", "https://frontend-deploy-test-rosy.vercel.app");
    }
}