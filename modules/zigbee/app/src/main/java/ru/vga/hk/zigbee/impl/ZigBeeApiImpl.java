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

package ru.vga.hk.zigbee.impl;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.ProcessManager;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.exception.ExceptionUtils;
import ru.vga.hk.zigbee.api.ZigBeeApi;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

public class ZigBeeApiImpl implements ZigBeeApi {

    private final MqttClient client;

    private final Logger logger = LoggerFactory.getLogger(ZigBeeApiImpl.class);

    private final ProcessManager comqttProcessManager;

    private final ProcessManager zigbee2mqttProcessManager;

    public ZigBeeApiImpl() throws MqttException {
        comqttProcessManager = new ProcessManager("comqtt", new String[]{"./comqtt",  "--conf=config.yaml"}, "comqtt", new File("externalPrograms/comqtt"));
        comqttProcessManager.startProcess();
        zigbee2mqttProcessManager = new ProcessManager("zigbee2mqtt", new String[]{"sudo",  "../../node/dist/bin/pnpm", "start"}, "node", new File("externalPrograms/zigbee2mqtt/dist"));
        zigbee2mqttProcessManager.startProcess();
        var clientId = UUID.randomUUID().toString();
        client = new MqttClient("tcp://localhost:1883",clientId);
        Environment.getPublished(Configuration.class).registerDisposable(this);
        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client.connect(options);
        logger.info("Connected to mqtt server");
    }

    @Override
    public void subscribe(String topic, Consumer<String> listener) {
        ExceptionUtils.wrapException(() ->{
            client.subscribe("zigbee2mqtt/%s".formatted(topic), (s, mqttMessage) -> {
                listener.accept(new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));
            });
            logger.info("Subscribed to topic '{}'",topic);
        });

    }

    @Override
    public void sendCommand(String topic, String command) {
        ExceptionUtils.wrapException(() ->{
            client.publish("zigbee2mqtt/%s".formatted(topic), command.getBytes(StandardCharsets.UTF_8), 0, false);
        });
        logger.info("Command was sent to topic '{}'",topic);
    }

    @Override
    public void dispose() throws Exception {
        try {
            zigbee2mqttProcessManager.stopProcess();
            comqttProcessManager.stopProcess();
            if(client.isConnected()) {
                client.disconnect();
            }
            logger.info("Disconnected from mqtt server");
        } finally {
            Environment.unpublish(ZigBeeApi.class);
            client.close();
            logger.info("Closed connection from mqtt server");
        }
    }
}
