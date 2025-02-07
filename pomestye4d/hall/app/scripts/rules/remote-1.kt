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

package rules

import items.iRadioBathroom
import items.mpd1
import items.remote1
import ru.vga.hk.core.api.builder.When

val ruleRemote1 = When(remote1.action()) {
//    when(payload.action){
//        "1_single" -> mpd1.play()
//        "2_single" -> mpd1.stop()
//    }
    when (payload.action) {
        "1_single" -> iRadioBathroom.play(0)
        "4_single" -> iRadioBathroom.stop()
        "1_double" -> iRadioBathroom.increaseVolume()
        "2_double" -> iRadioBathroom.decreaseVolume()
        "2_single" -> iRadioBathroom.play(1)
        "3_single" -> iRadioBathroom.nextSong()
    }
}