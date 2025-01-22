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

val zigbee = zigbee()

class Co2DeviceMeasurements{
    lateinit var eco2: Number
}

val co2Device = zigbee.device("0x00158d0000dacd76", Co2DeviceMeasurements::class, EmptyClass::class)

val co2LevelItem = co2Device.item("eco2")

class RozetkaCmd {
    lateinit var state: String
}
val rozetka2 = zigbee.device("Розетка 2", EmptyClass::class, RozetkaCmd::class)