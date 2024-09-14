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

package ru.vga.hk.core.impl.boot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vga.hk.core.api.common.Disposable;
import ru.vga.hk.core.api.environment.Configuration;
import ru.vga.hk.core.api.event.EventBus;
import ru.vga.hk.core.api.environment.Environment;
import ru.vga.hk.core.api.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import static java.nio.file.StandardWatchEventKinds.*;

public class ConfigurationHandler implements Disposable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private  WatchService watcher;

    private Thread thread;

    @Override
    public void dispose(){
        try {
            if(watcher != null) {
                watcher.close();
            }
            if(thread != null){
                thread.interrupt();
            }
        } catch (IOException e) {
            logger.error("unable to stop watch service", e);
        }
    }
    public void init(){
        ExceptionUtils.wrapException(()->{
            watcher = FileSystems.getDefault().newWatchService();
            refresh();
            thread = new Thread("configuration-watch-service"){
                {
                    setDaemon(true);
                }
                @Override
                public void run() {

                    try {
                        new File("config").toPath().register(watcher,
                                ENTRY_CREATE);
                    } catch (IOException e) {
                        logger.error("unable to register watch service", e);
                    }
                    WatchKey key;
                    for (;;) {
                        try {
                            key = watcher.take();
                            for (WatchEvent<?> event : key.pollEvents()) {
                                WatchEvent.Kind<?> kind = event.kind();

                                if (kind == OVERFLOW) {
                                    continue;
                                }
                                Environment.getPublished(EventBus.class).clear();
                                System.gc();
                                Thread.sleep(2000);
                                refresh();
                            }
                            boolean valid = key.reset();
                            if (!valid) {
                                break;
                            }
                        } catch (Throwable t){
                            logger.warn("unable to watch changes", t);
                        }
                    }
                }
            };
            thread.start();
        });

    }
    public void refresh() {
        try {
            logger.info("refreshing configuration");
            Environment.getPublished(Configuration.class).clear();
            var dir = new File("config");
            var file = Stream.of(dir.listFiles()).max(Comparator.comparing(File::getName)).orElse(null);
            if(file == null){
                logger.warn("unable to find configuration file");
                return;
            }
            logger.info("using %s".formatted(file.getName()));
            Arrays.stream(dir.listFiles()).forEach(it ->{
                if(!it.equals(file)){
                    it.delete();
                }
            });
            var classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            try {
                var jarFile = new JarFile(file);
                var classNames = new ArrayList<String>();
                jarFile.stream().forEach(e -> {
                    if(e.getName().equals("application.properties")){
                        ExceptionUtils.wrapException(()->{
                            try(var is = classLoader.getResourceAsStream(e.getName())){
                                Environment.getPublished(Configuration.class).load(new InputStreamReader(is, StandardCharsets.UTF_8));
                            }
                        });
                    }
                    if (e.getName().endsWith(".class") & !e.getName().contains("$")) {
                        classNames.add(e.getName().replace(".class", "").replace("/", "."));
                    }
                });
                for (var cn : classNames) {
                    Class.forName(cn, true, classLoader);
                }
            } finally {
                classLoader.close();
            }

            logger.info("configuration was refreshed");

        } catch (Throwable t) {
            logger.error("unable to refresh configuration", t);
        }
    }
}
