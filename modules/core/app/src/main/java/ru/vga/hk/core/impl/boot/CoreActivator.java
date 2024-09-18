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

package ru.vga.hk.core.impl.boot;

import ru.vga.hk.core.api.boot.Activator;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.storage.RrdStorage;
import ru.vga.hk.core.impl.event.EventBusImpl;
import ru.vga.hk.core.impl.storage.RrdStorageImpl;

public class CoreActivator implements Activator {
    @Override
    public void configure() throws Exception {
        Environment.publish(new Configuration());
        Environment.publish(EventBus.class, new EventBusImpl());
        Environment.publish(new ConfigurationHandler());
    }

    @Override
    public void activate() throws Exception {
        Environment.getPublished(ConfigurationHandler.class).init();
    }

    @Override
    public double getPriority() {
        return 0;
    }
}
