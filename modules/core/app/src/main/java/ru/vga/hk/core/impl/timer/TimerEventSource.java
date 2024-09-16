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

package ru.vga.hk.core.impl.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.event.EventSource;
import ru.vga.hk.core.api.timer.TimerEvent;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerEventSource implements EventSource<TimerEvent> {
    private final static AtomicInteger counter = new AtomicInteger(0);
    private final Timer timer;
    private final String id;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public TimerEventSource(String prefix, int delayInSeconds, int periodInSeconds){
        id = (prefix == null? "anonymous-timer": prefix)+counter.incrementAndGet();
        timer = new Timer(id, true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                var timerEvent = new TimerEvent();
                timerEvent.setEventDate(Instant.now());
                Environment.getPublished(EventBus.class).publishEvent(getId(), timerEvent);
            }
        }, TimeUnit.SECONDS.toMillis(delayInSeconds), TimeUnit.SECONDS.toMillis(periodInSeconds));
        log.info("scheduled timer " + this);
    }
    @Override
    public void dispose() {
        timer.cancel();
        log.info("disposed timer " + this);
    }

    @Override
    public String getId() {
        return id;
    }
}
