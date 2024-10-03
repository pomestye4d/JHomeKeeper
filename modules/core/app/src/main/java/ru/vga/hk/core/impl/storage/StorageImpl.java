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
import org.rrd4j.core.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.common.Pair;
import ru.vga.hk.core.api.exception.ExceptionUtils;
import ru.vga.hk.core.api.storage.Storage;
import ru.vga.hk.core.api.storage.StorageStrategy;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class StorageImpl implements Storage, Disposable {

    private static final Logger log = LoggerFactory.getLogger(StorageImpl.class);
    private final Map<String, RrdDb> databases = new ConcurrentHashMap<>();
    private final Map<String,String> items2Strategy =new ConcurrentHashMap<>();
    private final StorageStrategy defaultStrategy;

    private final Map<String, StorageStrategy> strategies = new ConcurrentHashMap<>();

    public StorageImpl() {
        var df  = new RrdStorageStrategy("default");
        df.def.setStep(60);
        df.def.addArchive(ConsolFun.AVERAGE, 0.9, 1, 60);
        df.def.addArchive(ConsolFun.AVERAGE, 0.9, 60, 24);
        df.def.addArchive(ConsolFun.AVERAGE, 0.9, 60*24, 30);
        df.def.addArchive(ConsolFun.AVERAGE, 0.9, 60*24*30, 12);
        defaultStrategy = df;
    }

    @Override
    public void dispose() throws Exception {
        databases.values().forEach(it -> {
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
    public void addStrategy(StorageStrategy strategy) {
        strategies.put(strategy.getId(), strategy);
    }

    @Override
    public void assignStrategy(String itemId, String strategyId) {
        items2Strategy.put(itemId, strategyId);
    }

    @Override
    public void store(String id, Number value) {
        try {
            var cid = cleanupId(id);
            var db = databases.computeIfAbsent(cid, (i) -> ExceptionUtils.wrapException(() -> {
                String rrdUri = "%s.rrd".formatted(cid);
                var file = new File("data/%s".formatted(rrdUri));
                if(file.exists()){
                    return RrdDb.of(file.getAbsolutePath());
                }
                var strategy = (RrdStorageStrategy) (this.items2Strategy.containsKey(id)? this.strategies.get(this.items2Strategy.get(id)) : this.defaultStrategy);
                var rrdDef = new RrdDef(file.getAbsolutePath(), 0,strategy.def.getStep());
                rrdDef.addDatasource("data", DsType.GAUGE, 3600, Double.NaN, Double.NaN);
                Stream.of(strategy.def.getArcDefs()).forEach(it -> rrdDef.addArchive(ConsolFun.AVERAGE, it.getXff(), it.getSteps(), it.getRows()));
                return RrdDb.of(rrdDef);
            }));
            var sample = db.createSample();
            sample.setTime(Util.getTimestamp());
            sample.setValue("data", value.doubleValue());
            sample.update();
            log.debug("stored %s with %s".formatted(id, value));
        } catch (IOException e) {
            log.error("unable to update %s with %s".formatted(id, value));
        }
    }



    @Override
    public List<Pair<Instant, Double>> getData(String id, Instant from, Instant to) {
        return ExceptionUtils.wrapException(() -> {
            var db = databases.get(id);
            if(db == null){
                return Collections.emptyList();
            }
            var start = Util.getTimestamp(Date.from(from));
            var end = Util.getTimestamp(Date.from(to));
            var res = db.createFetchRequest(ConsolFun.AVERAGE, start, end, (end-start)/100).fetchData();
            var tss = res.getTimestamps();
            var values = res.getValues("data");
            var result = new ArrayList<Pair<Instant, Double>>();
            for(int i=0; i<tss.length; i++) {
                var ts = Util.getDate(tss[i]).toInstant();
                var value = values[i];
                result.add(new Pair<>(ts, value));
            }
            return result;
        });
    }

}