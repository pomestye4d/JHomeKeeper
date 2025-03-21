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

import org.rrd4j.ConsolFun
import ru.vga.hk.core.api.common.BasicEventSource
import ru.vga.hk.core.api.common.ButtonHandler
import ru.vga.hk.core.api.common.HasId
import ru.vga.hk.core.api.environment.Configuration
import ru.vga.hk.core.api.environment.Environment
import ru.vga.hk.core.api.event.EventBus
import ru.vga.hk.core.api.event.EventSource
import ru.vga.hk.core.api.httpItem.HttpItemOptions
import ru.vga.hk.core.api.rest.RestEvent
import ru.vga.hk.core.api.storage.Storage
import ru.vga.hk.core.api.storage.StorageStrategy
import ru.vga.hk.core.api.timer.TimerEvent
import ru.vga.hk.core.api.ui.*
import ru.vga.hk.core.impl.httpItem.HttpItem
import ru.vga.hk.core.impl.storage.RrdStorageStrategy
import ru.vga.hk.core.impl.timer.TimerEventSource

var uiElementIndex = 0

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

fun httpItem(id: String, path: String, periodInSeconds: Int, customizer: ((options: HttpItemOptions) -> Unit)? = null):HttpItem {
    val httpItem = HttpItem(id, path, periodInSeconds, customizer)
    if(httpItem.storageStrategyId != null){
        Environment.getPublished(Storage::class.java).assignStrategy(id, httpItem.storageStrategyId)
    }
    Environment.getPublished(Configuration::class.java).registerDisposable(httpItem)
    return httpItem
}

fun configProperty(name: String): String? {
    return Environment.getPublished(Configuration::class.java).getProperty(name)
}

fun rrdStorageStrategy(configure:RrdStrategyBuilder.() -> Unit): String {
    val strategy = RrdStorageStrategy(StorageStrategy.counter.incrementAndGet().toString())
    val builder = RrdStrategyBuilder(strategy)
    builder.configure()
    Environment.getPublished(Storage::class.java).addStrategy(strategy)
    return strategy.id
}

fun button(id:String, label: String, handler:()->Unit):ButtonUiElement {
    Environment.getPublished(Configuration::class.java).registerButtonHandler(object : ButtonHandler {
        override fun getId(): String {
            return id
        }

        override fun handle() {
            handler()
        }
    })
    return ButtonUiElement(id, label)
}

fun label(id:String, label: String) = LabelUiElement(id, label)

@DslMarker
annotation class UiBuilderMarker

@UiBuilderMarker
class ChartBuilder(private val chart: ChartUiElement) {
    fun plot(name: String, item:HasId) {
        chart.plots.add(Plot(item.id, name))
    }
}

@UiBuilderMarker
class RowBuilder(private val row: GridRowElement) {
    fun column(elm:BaseUiElement, vararg sizes: Pair<ScreenSize, Int>) {
        row.columns.add(GridColumnElement(elm, sizes.map { ru.vga.hk.core.api.common.Pair(it.first, it.second) }))
    }
}

@UiBuilderMarker
class GridBuilder(private val grid: GridItem) {
    fun row(configure: RowBuilder.() -> Unit) {
        val row = GridRowElement();
        RowBuilder(row).configure()
        grid.rows.add(row)
    }
}

@UiBuilderMarker
class GroupBuilder(private val group: UiGroup) {
    fun chart(name: String, configure: ChartBuilder.() -> Unit) {
        uiElementIndex++
        val chart = ChartUiElement("chart-${uiElementIndex}", name)
        group.elements.add(chart)
        ChartBuilder(chart).configure()
    }
    fun grid(name:String, configure: GridBuilder.() -> Unit) {
        uiElementIndex++
        val grid = GridItem("grid-${uiElementIndex}", name)
        group.elements.add(grid)
        GridBuilder(grid).configure()
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
    Environment.getPublished(Configuration::class.java).setUi{
        uiElementIndex = 0
        val uiGroups = arrayListOf<UiGroup>();
        UiBuilder(uiGroups).configure()
        uiGroups
    }
}


@UiBuilderMarker
class RrdStrategyBuilder(private val strategy: RrdStorageStrategy) {
    fun definition(stepInSeconds: Int){
       strategy.def.step = 0
    }
    fun archive(steps:Int, rows:Int){
        strategy.def.addArchive(ConsolFun.AVERAGE, 1.0, steps, rows)
    }
}