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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class UiButtonHandler implements HttpHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public void handle(HttpExchange exchange) {
        if (!"POST".equals(exchange.getRequestMethod())) {
            log.warn("invalid request method: {}", exchange.getRequestMethod());
            exchange.close();
            return;
        }
        try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);
            var req = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
            var buttonId = req.getAsJsonPrimitive("id").getAsString();
            try{
                var handler = Environment.getPublished(Configuration.class).getButtonHandler(buttonId);
                if(handler == null) {
                    throw new Exception("no button handler found for id " + buttonId);
                }
                handler.handle();
                exchange.getResponseBody().write("{\"result\": \"OK\"}".getBytes(StandardCharsets.UTF_8));
            } catch (Throwable t){
                log.error("unable to handle button");
                exchange.getResponseBody().write("{\"result\": \"ERROR\"}".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Throwable t){
            log.error("unable to handle request", t);
        }
    }
}
