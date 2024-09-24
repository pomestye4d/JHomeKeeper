/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: TorDi
 *****************************************************************/

package ru.vga.hk.core.impl.webserver;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer implements Disposable {

    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private final HttpServer delegate;

    private final ExecutorService executorService;

    public WebServer() throws IOException {
        executorService = Executors.newFixedThreadPool(5);
        delegate = HttpServer.create(new InetSocketAddress(Integer.parseInt(Environment.getPublished(Configuration.class).getProperty("webServer.port", "8080"))), 10);
        delegate.setExecutor(executorService);
        delegate.createContext("/api", new ApiHandler());
        delegate.createContext("/ui/config", new UiConfigHandler());
        delegate.start();
    }

    @Override
    public void dispose() {
        executorService.shutdownNow();
        delegate.stop(10);
    }
}
