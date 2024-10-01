/*
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.vga.hk.core.impl.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.rest.RestCallback;
import ru.vga.hk.core.api.rest.RestEvent;

import java.nio.charset.StandardCharsets;

public class ApiHandler implements HttpHandler {
    private final Logger log = LoggerFactory.getLogger(ApiHandler.class);
    @Override
    public void handle(HttpExchange exchange) {
        if (!"GET".equals(exchange.getRequestMethod()) && !"POST".equals(exchange.getRequestMethod())) {
            exchange.close();
            return;
        }
        try (exchange) {
            var event = new RestEvent(new RestCallback() {
                @Override
                public void send(String message) throws Exception {
                    exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
                }
            });
            event.setRequestPath(exchange.getRequestURI().getPath().substring(8));
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);
            Environment.getPublished(EventBus.class).publishEvent("rest-%s".formatted(event.getRequestPath()), event);
        } catch (Throwable t){
            log.error("unable to handler request", t);
        }
    }
}
