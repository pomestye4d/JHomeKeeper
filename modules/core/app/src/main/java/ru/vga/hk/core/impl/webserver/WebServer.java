/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: TorDi
 *****************************************************************/

package ru.vga.hk.core.impl.webserver;

import com.sun.net.httpserver.HttpServer;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.rest.RestEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer implements Disposable {

    private final HttpServer delegate;

    private final ExecutorService executorService;

    public WebServer() throws IOException {
        executorService = Executors.newFixedThreadPool(5);
        delegate = HttpServer.create(new InetSocketAddress(Integer.parseInt(Environment.getPublished(Configuration.class).getProperty("webServer.port", "8080"))), 10);
        delegate.setExecutor(executorService);
        delegate.createContext("/", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod()) && !"POST".equals(exchange.getRequestMethod())) {
                exchange.close();
                return;
            }
            try (exchange) {
                var event = new RestEvent();
                event.setRequestPath(exchange.getRequestURI().getPath().substring(1));
                Environment.getPublished(EventBus.class).publishEvent("rest-%s".formatted(event.getRequestPath()), event);
            }
        });
        delegate.start();
    }

    @Override
    public void dispose() {
        executorService.shutdownNow();
        delegate.stop(10);
    }
}
