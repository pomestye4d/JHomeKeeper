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

import ru.vga.hk.core.api.common.BasicEventSource
import ru.vga.hk.core.api.environment.Configuration
import ru.vga.hk.core.api.environment.Environment
import ru.vga.hk.core.api.event.EventBus
import ru.vga.hk.core.api.event.EventSource
import ru.vga.hk.core.api.httpItem.HttpItemOptions
import ru.vga.hk.core.api.rest.RestEvent
import ru.vga.hk.core.api.storage.RrdStorage
import ru.vga.hk.core.api.storage.StorageStrategy
import ru.vga.hk.core.api.timer.TimerEvent
import ru.vga.hk.core.api.ui.GraphPlot
import ru.vga.hk.core.api.ui.GraphUiElement
import ru.vga.hk.core.api.ui.UiGroup
import ru.vga.hk.core.impl.httpItem.HttpItem
import ru.vga.hk.core.impl.timer.TimerEventSource

fun timer(name: String, delayInSeconds: Int, periodInSeconds: Int): EventSource<TimerEvent> {
    val result = TimerEventSource(name, delayInSeconds, periodInSeconds)
    Environment.getPublished(Configuration::class.java).registerDisposable(result)
    return result
}

fun timer(periodInSeconds: Int): EventSource<TimerEvent> {
    val result = TimerEventSource(null, 0, periodInSeconds)
    Environment.getPublished(Configuration::class.java).registerDisposable(result)
    return result
}

fun <E> When(eventSource: EventSource<E>, handler: E.() -> Unit) {
    Environment.getPublished(EventBus::class.java).registerRule(eventSource) {
        handler.invoke(it)
    }
}

fun rest(path: String, handler: RestEvent.() -> Unit) {
    Environment.getPublished(EventBus::class.java).registerRule(BasicEventSource<RestEvent>("rest-${path}")) {
        handler.invoke(it)
    }
}

fun httpItem(id: String, path: String, periodInSeconds: Int, customizer: ((options: HttpItemOptions) -> Unit)? = null):String {
    val httpItem = HttpItem(id, path, periodInSeconds, customizer)
    Environment.getPublished(Configuration::class.java).registerDisposable(httpItem)
    return id
}

fun configProperty(name: String): String? {
    return Environment.getPublished(Configuration::class.java).getProperty(name)
}

fun storageStrategy(id: String, periodInSeconds: Int): String {
    Environment.getPublished(RrdStorage::class.java)
        .addStrategy(id, StorageStrategy().also { it.periodInSeconds = periodInSeconds })
    return id
}

@DslMarker
annotation class UiBuilderMarker

@UiBuilderMarker
class GraphBuilder(private val graph: GraphUiElement) {
    fun plot(name: String, itemId:String) {
        graph.plots.add(GraphPlot(name, itemId))
    }
}

@UiBuilderMarker
class GroupBuilder(private val group: UiGroup) {
    fun graph(name: String, configure: GraphBuilder.() -> Unit) {
        val graph = GraphUiElement(name)
        group.elements.add(graph)
        GraphBuilder(graph).configure()
    }
}

@UiBuilderMarker
class UiBuilder(private val groups: MutableList<UiGroup>) {
    fun group(name: String, configure: GroupBuilder.() -> Unit) {
        val group = UiGroup(name)
        groups.add(group)
        val gb = GroupBuilder(group)
        gb.configure()
    }
}

@UiBuilderMarker
fun ui(configure: UiBuilder.() -> Unit) {
    UiBuilder(Environment.getPublished(Configuration::class.java).ui).configure()
}