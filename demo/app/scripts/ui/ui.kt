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

package ui

import items.*
import ru.vga.hk.core.api.builder.button
import ru.vga.hk.core.api.builder.label
import ru.vga.hk.core.api.builder.ui
import ru.vga.hk.core.api.ui.ScreenSize

val ui = ui {
    group("Charts"){
        chart("Test"){
            plot("Test", httpItem1)
        }

//        chart("CO2"){
//            plot("CO2", co2LevelItem)
//        }
//        grid("Розетка 2") {
//            row {
//                column(button("turn-on", "Вкл"){
//                    rozetka2.sendCommand { state="ON" }
//                }, ScreenSize.SMALL to 50, ScreenSize.LARGE to 10)
//                column(button("turn-off", "Выкл"){
//                    rozetka2.sendCommand { state="OFF" }
//                }, ScreenSize.SMALL to 50, ScreenSize.LARGE to 10)
//            }
//        }
    }
    group("Bathroom"){
        chart("Test 2"){
            plot("Test 2", httpItem2)
        }
        grid("Player") {
            row {
                column(button("play", "Play"){
                    mpd1.play()
                }, ScreenSize.SMALL to 50, ScreenSize.LARGE to 10)
                column(button("stop", "Stop"){
                    mpd1.stop()
                }, ScreenSize.SMALL to 50, ScreenSize.LARGE to 10)
            }
        }
    }
}