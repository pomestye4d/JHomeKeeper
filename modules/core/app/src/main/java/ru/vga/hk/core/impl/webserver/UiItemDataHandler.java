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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.storage.Storage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class UiItemDataHandler implements HttpHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            log.warn("invalid request method: {}", exchange.getRequestMethod());
            exchange.close();
            return;
        }
        try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);
            var req = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8), JsonObject.class);
            var itemId = req.getAsJsonPrimitive("itemId").getAsString();
            var startDate = Instant.parse(req.getAsJsonPrimitive("startDate").getAsString());
            var endDate = req.has("endDate")? Instant.parse(req.getAsJsonPrimitive("endDate").getAsString()): Instant.now();
            var data = Environment.getPublished(Storage.class).getData(itemId, startDate, endDate);
            var arr = new JsonArray();
            for (var item : data) {
                var obj = new JsonObject();
                obj.addProperty("date", item.first().toString());
                obj.addProperty("value", item.second());
                arr.add(obj);
            }
            exchange.getResponseBody().write(new Gson().toJson(arr).getBytes(StandardCharsets.UTF_8));
        } catch (Throwable t){
            log.error("unable to handle request", t);
        }
    }
}
