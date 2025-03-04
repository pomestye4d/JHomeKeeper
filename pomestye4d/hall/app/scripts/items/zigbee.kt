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

package items

import ru.vga.hk.zigbee.api.builder.EmptyClass
import ru.vga.hk.zigbee.api.builder.zigbee

val zigbeeBridge = zigbee()

class Co2DeviceMeasurements{
    lateinit var eco2: Number
    lateinit var temperature: Number
    lateinit var humidity: Number
    lateinit var battery: Number
}

class TemperatureMeasurements{
    lateinit var temperature: Number
    lateinit var humidity: Number
    lateinit var battery: Number
}

class RemoteMeasurements{
    lateinit var battery: Number
}

val bedroomCo2Sensor = zigbeeBridge.device("0x00158d0000dacd76", Co2DeviceMeasurements::class, EmptyClass::class)

val bedroomCo2LevelItem = bedroomCo2Sensor.item("eco2")
val bedroomHumidityItem = bedroomCo2Sensor.item("humidity")
val bedroomTemperatureItem = bedroomCo2Sensor.item("temperature")
val bedroomBattery = bedroomCo2Sensor.item("battery")

val nataTemperatureSensor = zigbeeBridge.device("0x00124b00226870c1", TemperatureMeasurements::class, EmptyClass::class)
val nataHumidityItem = nataTemperatureSensor.item("humidity")
val nataTemperatureItem = nataTemperatureSensor.item("temperature")
val nataBatteryItem = nataTemperatureSensor.item("battery")