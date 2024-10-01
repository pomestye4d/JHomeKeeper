/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: TorDi
 *****************************************************************/

package ru.vga.hk.core.impl.webserver;

import com.sun.net.httpserver.HttpServer;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;

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
        delegate.createContext("/extApi", new ApiHandler());
        delegate.createContext("/ui/config", new UiConfigHandler());
        delegate.createContext("/ui/itemData", new UiItemDataHandler());
        delegate.start();
    }

    @Override
    public void dispose() {
        executorService.shutdownNow();
        delegate.stop(10);
    }
}
