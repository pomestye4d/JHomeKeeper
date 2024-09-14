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

package ru.vga.hk.core.impl.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.event.EventHandler;
import ru.vga.hk.core.api.event.EventSource;
import ru.vga.hk.core.api.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBusImpl implements EventBus, Disposable {

    private List<EventSource<?>> eventSources = Collections.synchronizedList(new ArrayList<>());

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, EventHandler<?>> handlers = new ConcurrentHashMap<>();
    @Override
    public void registerEventSource(EventSource<?> source) {
        eventSources.add(source);
    }

    @Override
    public <E> void registerRule(EventSource<E> eventSource, EventHandler<E> handler) {
        handlers.put(eventSource.getId(), handler);
    }

    @Override
    public <E> void publishEvent(String sourceId, E event) {
        var handler = handlers.get(sourceId);
        if(handler == null){
           return;
        }
        ((EventHandler<E>)handler).handle(event);
    }


    @Override
    public void clear() {
        for(var src: eventSources){
            ExceptionUtils.wrapException(()->{
                src.dispose();
            });
        }
        eventSources.clear();
        handlers.clear();
    }

    @Override
    public void dispose() throws Exception {
        clear();
    }
}
