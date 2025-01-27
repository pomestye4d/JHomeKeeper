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

package ru.vga.hk.zigbee.api.builder

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import ru.vga.hk.core.api.common.HasId
import ru.vga.hk.core.api.environment.Environment
import ru.vga.hk.core.api.event.EventBus
import ru.vga.hk.core.api.event.EventSource
import ru.vga.hk.core.api.storage.Storage
import ru.vga.hk.zigbee.api.ZigBeeApi
import ru.vga.hk.zigbee.api.ZigBeeEvent
import ru.vga.hk.zigbee.impl.ZigBeeApiImpl
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty

class EmptyClass
annotation class ZigBeeAction

class ZigBeeDevice<M, C : Any>(private val deviceId: String, private val commandClass: KClass<C>) {
    internal var lastMessageDate: Instant? = null
    internal var lastMessageValue: M? = null

    fun item(paramId: String): HasId {
        return object : HasId {
            override fun getId(): String {
                return "$deviceId-$paramId"
            }

        }
    }

    fun action():EventSource<ZigBeeEvent<M>> {
        return object : EventSource<ZigBeeEvent<M>> {
            override fun dispose() {
                //noop
            }

            override fun getId(): String {
                return "zigbee-$deviceId"
            }

        }
    }

    fun lastMessage(): Pair<M, Instant>? {
        return lastMessageDate?.let {
            Pair(lastMessageValue!!, it)
        }
    }

    fun sendCommand(command: C.() -> Unit) {
        val cmd = commandClass.constructors.first().call()
        cmd.command()
        val data = JsonObject()
        cmd.javaClass.fields.forEach { f ->
            val value = f.get(cmd) ?: return@forEach
            if (value is Number) {
                data.addProperty(f.name, value)
            } else if (value is String) {
                data.addProperty(f.name, value)
            }
        }
        val payload = Gson().toJson(data)
        Environment.getPublished(ZigBeeApi::class.java).sendCommand("${deviceId}/set", payload)
    }

}

class ZigBeeExt {
    private val logger = LoggerFactory.getLogger(this::class.java)
    fun <M : Any, C : Any> device(
        id: String,
        messageClass: KClass<M>,
        commandClass: KClass<C>
    ): ZigBeeDevice<M, C> {
        val zigBeeDevice = ZigBeeDevice<M, C>(id, commandClass)
        if (messageClass != EmptyClass::class) {
            Environment.getPublished(ZigBeeApi::class.java).subscribe(id) {
                try {
                    val data = Gson().fromJson(
                        it,
                        JsonObject::class.java
                    )
                    val result = messageClass.constructors.first().call()
                    var hasAction = false
                    messageClass.members.forEach { f ->
                        val value = data.get(f.name)
                        value?.let {
                            if (f is KMutableProperty<*>) {
                                if(f.returnType.classifier?.equals(String::class) == true) {
                                    f.setter.call(result, value.asString)
                                } else {
                                    f.setter.call(result, value.asDouble)
                                }
                            }
                            if(f.annotations.none { it is ZigBeeAction}) {
                                Environment.getPublished(Storage::class.java).store("$id-${f.name}", value.asNumber)
                            } else {
                                hasAction = true
                            }
                        }
                    }
                    if(hasAction){
                        Environment.getPublished(EventBus::class.java).publishEvent("zigbee-$id", ZigBeeEvent(result))
                    }
                    zigBeeDevice.lastMessageDate = Instant.now()
                    zigBeeDevice.lastMessageValue = result
                } catch (e: Throwable) {
                    logger.error("unable to process measurements", e)
                }
            }
        }
        return zigBeeDevice;
    }

}

fun zigbee(): ZigBeeExt {
    Environment.publish(ZigBeeApi::class.java, ZigBeeApiImpl())
    return ZigBeeExt()
}

