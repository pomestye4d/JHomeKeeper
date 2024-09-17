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

package ru.vga.hk.tg.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.vga.hk.core.api.common.BasicEventSource;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.tg.api.TgBotApi;
import ru.vga.hk.tg.api.TgMessageEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TgBotApiImpl extends TelegramLongPollingBot implements TgBotApi, Disposable {
    private int connectCount;
    private final String username;
    private final int RECONNECT_PAUSE = 10000;
    private final Map<String, String> patterns = new ConcurrentHashMap<>();
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BotSession session;

    public TgBotApiImpl(String username, String token) throws Exception {
        super(token);
        this.username = username;
        Environment.getPublished(Configuration.class).registerDisposable(this);
        if(token == null || token.isEmpty()){
            return;
        }
        botConnect();
    }

    private void botConnect() throws Exception {
        connectCount++;
        try {
            var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            session = telegramBotsApi.registerBot(this);
            log.info("TelegramAPI started. Look for messages");
        } catch (Exception e) {
            log.error("Cant Connect. Pause " + RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.getMessage());
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                //noops
                return;
            }
            if(connectCount > 3) {
                throw new Exception("unable to connect to tg");
            }
            botConnect();

        }
    }

    public BasicEventSource<TgMessageEvent> addPattern(String pattern) {
        var id = "tg-bot-%s-%s".formatted(username, pattern);
        patterns.put(pattern, id);
        return new BasicEventSource<>(id);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            log.debug("got message %s from chat %s".formatted(messageText, chatId));
            var id = patterns.entrySet().stream().filter(it -> messageText.equals(it.getKey()) || messageText.matches(it.getKey()))
                    .map(Map.Entry::getValue).findFirst().orElse(null);
            if(id != null){
                log.debug("message matches a pattern");
                var event = new TgMessageEvent();
                event.setApi(this);
                Environment.getPublished(EventBus.class).publishEvent(id, event);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }


    @Override
    public void dispose(){
        log.info("disposed");
        if(session != null && session.isRunning()){
            session.stop();
            log.info("session stopped");
        }
    }
}
