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

package ru.vga.hk.core.api.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.ButtonHandler;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.ui.UiGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

public class Configuration extends Properties {
    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Supplier<List<UiGroup>> ui;

    private final Map<String, ButtonHandler> buttonHandlers = new HashMap<>();

    @Override
    public synchronized String toString() {
        return getClass().getName();
    }

    public void registerDisposable(Disposable d){
        log.info("registered disposable " + d);
        disposables.add(d);
    }

    public void setUi(Supplier<List<UiGroup>> ui){
        this.ui = ui;
    }

    public void registerButtonHandler(ButtonHandler handler){
        buttonHandlers.put(handler.getId(), handler);
    }

    public ButtonHandler getButtonHandler(String id){
        return buttonHandlers.get(id);
    }
    public List<UiGroup> getUi() {
        return ui == null? Collections.emptyList(): ui.get();
    }

    public void cleanup(){
        ui = null;
        buttonHandlers.clear();
        Collections.reverse(disposables);
        for(var disp: disposables){
            try{
                disp.dispose();
                log.info("disposed " + disp);
            } catch (Throwable t){
                log.error("unable to dispose " + disp, t);
            }
        }
    }
}
