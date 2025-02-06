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

package ru.vga.hk.iradio.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.exception.ExceptionUtils;
import ru.vga.hk.iradio.api.IRadioApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class IradioApiImpl implements IRadioApi {
    private String ip;
    private int port;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public IradioApiImpl(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private void makePostRequest(String path, String body){
        ExceptionUtils.wrapException(() ->{
            logger.debug("making request %s: %s".formatted(path, body));
            var url = new URI("http://%s:%s/radio/%s".formatted(ip, port, path)).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
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
        });

    }

    @Override
    public void stop() {
        makePostRequest("stop", null);
    }

    @Override
    public void play(int radioIdx) {
        makePostRequest("play", "{\"id\": %s}".formatted(radioIdx));
    }

    @Override
    public void increaseVolume() {
        makePostRequest("increase-volume", null);
    }

    @Override
    public void decreaseVolume() {
        makePostRequest("decrease-volume", null);
    }

    @Override
    public void nextSong() {
        makePostRequest("next-song", null);
    }
}
