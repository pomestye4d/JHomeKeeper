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

package ru.vga.hk.core.impl.storage;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.exception.ExceptionUtils;
import ru.vga.hk.core.api.storage.RrdStorage;
import ru.vga.hk.core.api.storage.StorageStrategy;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RrdStorageImpl implements RrdStorage, Disposable {

    private static final Logger log = LoggerFactory.getLogger(RrdStorageImpl.class);
    private final Map<String, RrdDb> databases = new ConcurrentHashMap<>();
    private final StorageStrategy defaultStrategy;

    private final Map<String,StorageStrategy> strategies = new ConcurrentHashMap<>();
    public RrdStorageImpl(){
        this.defaultStrategy = new StorageStrategy();
        this.defaultStrategy.setPeriodInSeconds(Integer.parseInt(Environment.getPublished(Configuration.class).getProperty("rrd.periodInSeconds", "60")));
    }
    @Override
    public void dispose() throws Exception {
        databases.values().forEach(it ->{
            try {
                it.close();
            } catch (Throwable e) {
                log.error("unable to close rrd db", e);
            }
        });
        databases.clear();
    }

    private String cleanupId(String id) {
        return id.replace(" ", "_").replace(".", "_");
    }

    @Override
    public void store(String id, Number value, String strategy) {
        try {
            var cid = cleanupId(id);
            var db = databases.computeIfAbsent(cid, (i) -> ExceptionUtils.wrapException(() -> {
                String rrdUri = "%s.rrd".formatted(cid);
                var file = new File("data/%s".formatted(rrdUri));
                var step = strategy == null? defaultStrategy.getPeriodInSeconds(): strategies.get(strategy).getPeriodInSeconds();
                var rrdDef = new RrdDef(file.getAbsolutePath(), step);
                rrdDef.addDatasource("data", DsType.GAUGE, step, Double.NaN, Double.NaN);
                rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 5*step);
                return RrdDb.of(rrdDef);
            }));
            var sample = db.createSample();
            sample.setTime(System.currentTimeMillis());
            sample.setValue("data", value.doubleValue());
            sample.update();
            log.debug("stored %s with %s".formatted(id, value));
        } catch (IOException e) {
            log.error("unable to update %s with %s".formatted(id, value));
        }
    }

    @Override
    public void addStrategy(String id, StorageStrategy strategy) {
        strategies.put(id, strategy);
    }
}
