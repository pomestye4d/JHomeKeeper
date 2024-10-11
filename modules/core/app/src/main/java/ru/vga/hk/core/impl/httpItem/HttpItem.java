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

package ru.vga.hk.core.impl.httpItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.common.HasId;
import ru.vga.hk.core.api.common.Pair;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.event.EventSource;
import ru.vga.hk.core.api.exception.ExceptionUtils;
import ru.vga.hk.core.api.httpItem.HttpEvent;
import ru.vga.hk.core.api.httpItem.HttpItemOptions;
import ru.vga.hk.core.api.storage.Storage;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpItem implements Disposable, EventSource<Number>, HasId {

    private final Timer timer;

    private final String id;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final String storageStrategyId;

    public HttpItem(String id, String path, int periodInSeconds,  Consumer<HttpItemOptions> customizer) {
        this.id = id;
        timer = new Timer(id, true);
        HttpItemOptions options = new HttpItemOptions();
        options.setValueExtractor((value) -> value == null || value.isEmpty() ? null : new BigDecimal(value));
        options.setBodyBuilder(() -> null);
        if (customizer != null) {
            customizer.accept(options);
        }
        storageStrategyId = options.getStorageStrategyId();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ExceptionUtils.wrapException(() -> {
                    var url = new URI(path).toURL();
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    if (connection instanceof HttpsURLConnection) {
                        SSLContext sslContext = SSLContext.getInstance("SSL");//$NON-NLS-1$

                        sslContext.init(null, new TrustManager[]{trustManager},
                                new java.security.SecureRandom());

                        ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext
                                .getSocketFactory());
                    }
                    var body = options.getBodyBuilder().get();
                    if (body != null) {
                        try (OutputStream os = connection.getOutputStream()) {
                            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)).transferTo(os);
                        }
                    }
                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (InputStream is = connection.getInputStream()) {
                            is.transferTo(baos);
                        }
                        logger.warn("unable to get response from %s code = %s data = %s".formatted(path, responseCode, baos.toString(StandardCharsets.UTF_8)));
                        return;
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream is = connection.getInputStream()) {
                        is.transferTo(baos);
                    }
                    var response = baos.toString(StandardCharsets.UTF_8);
                    logger.debug("get response from %s code = %s data = %s".formatted(path, responseCode, response));
                    var value = options.getValueExtractor().apply(response);
                    if(value != null) {
                        logger.debug("storing value %s".formatted(value));
                        Environment.getPublished(Storage.class).store(id, value);
                        Environment.getPublished(EventBus.class).publishEvent(id, new HttpEvent());
                        return;
                    }
                    logger.warn("value is null");
                });
            }
        }, TimeUnit.SECONDS.toMillis(periodInSeconds), TimeUnit.SECONDS.toMillis(periodInSeconds));
    }

    public Pair<Instant, Double> getLastValue() {
        return Environment.getPublished(Storage.class).getLastValue(id);
    }


    @Override
    public void dispose() throws Exception {
        timer.cancel();
    }

    @Override
    public String getId() {
        return id;
    }

    private static final X509TrustManager trustManager =
            new X509TrustManager() {

                @Override
                public void checkClientTrusted(final X509Certificate[] chain,
                                               final String authType) {
                    // Empty
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain,
                                               final String authType) {
                    // Empty
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
}
