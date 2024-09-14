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

package ru.vga.hk.core.api.builder

import ru.vga.hk.core.api.environment.Configuration
import ru.vga.hk.core.api.event.EventBus
import ru.vga.hk.core.api.event.EventSource
import ru.vga.hk.core.api.environment.Environment
import ru.vga.hk.core.api.event.EventHandler
import ru.vga.hk.core.api.timer.TimerEvent
import ru.vga.hk.core.impl.timer.TimerEventSource

fun timer(name:String, delayInSeconds: Int, periodInSeconds:Int):EventSource<TimerEvent>{
    val result = TimerEventSource(name, delayInSeconds, periodInSeconds)
    Environment.getPublished(EventBus::class.java).registerEventSource(result);
    return result;
}

fun timer(periodInSeconds:Int):EventSource<TimerEvent> {
    val result = TimerEventSource(null, 0, periodInSeconds)
    Environment.getPublished(EventBus::class.java).registerEventSource(result);
    return result;
}

fun<E> When(eventSource:EventSource<E>, handler:E.()->Unit){
     Environment.getPublished(EventBus::class.java).registerRule(eventSource, EventHandler {
        handler.invoke(it)
     })
}

fun configProperty(name: String):String?{
    return Environment.getPublished(Configuration::class.java).getProperty(name)
}