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
import ru.vga.hk.core.api.common.Disposable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * General application environment configuration. Mainly used for accessing to
 * common published objects.
 */
public final class Environment {

    private final static AtomicInteger counter = new AtomicInteger(0);
    static final Logger log = LoggerFactory.getLogger(Environment.class);

    private static final Map<Class<?>, PublishedEntry<?>> publishedObjects = new ConcurrentHashMap<>();

    /**
     * Should be called once during application shutdown.
     */
    public static void dispose() {
        var list = publishedObjects.entrySet().stream().sorted(Comparator.comparing(it ->( (Map.Entry<Class<?>,PublishedEntry>)it)
                .getValue().order).reversed()).map(Map.Entry::getKey).toList();
        for (Class<?> cls : list) {
            unpublish(cls);
        }
        if (!publishedObjects.isEmpty()) {
            log.warn("not all objects were unpublished"); //$NON-NLS-1$
            publishedObjects.clear();
        }
    }

    /**
     * Method to publish object for common usage.
     *
     * @param <T>
     *            publishing object type
     * @param obj
     *            publishing object
     */
    public static <T> void publish(final T obj) {
        Class<?> cls = obj.getClass();
        if (publishedObjects.containsKey(cls)) {
            throw new IllegalArgumentException(String
                    .format("object of class %s already published", obj.getClass() //$NON-NLS-1$
                            .getName()));
        }
        publishedObjects.put(obj.getClass(), new PublishedEntry<>(obj));
        log.info("published " + obj); //$NON-NLS-1$
    }

    /**
     * Method to publish object for common usage.
     *
     * @param <T>
     *            publishing object type
     * @param cls
     *            publishing object class
     * @param obj
     *            publishing object
     */
    public static <T> void publish(final Class<? super T> cls, final T obj) {
        if (publishedObjects.containsKey(cls)) {
            throw new IllegalArgumentException(String
                    .format("object of class %s already published", cls.getName())); //$NON-NLS-1$
        }
        publishedObjects.put(cls, new PublishedEntry<>(obj));
        log.info(String.format("published %s, class = %s", obj, cls.getName())); //$NON-NLS-1$
    }

    /**
     * Method to remove object from common usage.
     *
     * @param cls
     *            object class
     */
    public static void unpublish(final Class<?> cls) {
        PublishedEntry<?> entry;
        entry = publishedObjects.get(cls);
        if (entry != null) {
            entry.dispose();
        }
        publishedObjects.remove(cls);
    }

    /**
     * Checks if any object of given class is published.
     *
     * @param cls
     *            object class
     * @return <code>true</code> if object is published and available
     */
    public static boolean isPublished(final Class<?> cls) {
        return publishedObjects.containsKey(cls);
    }

    /**
     * Returns published object. If no objects of given class is published then
     * {@link IllegalArgumentException} is thrown.
     *
     * @param <T>
     *            object type
     * @param cls
     *            published object class
     * @return published object instance
     */
    public static <T> T getPublished(final Class<T> cls) {
        @SuppressWarnings("unchecked")
        PublishedEntry<T> result = (PublishedEntry<T>) publishedObjects
                .get(cls);
        if (result == null) {
            throw new IllegalArgumentException(String
                    .format("object of class %s not published", cls.getName())); //$NON-NLS-1$
        }
        return result.object;
    }

    private static final class PublishedEntry<T> {
        final T object;

        final int order;

        PublishedEntry(final T obj) {
            order = counter.incrementAndGet();
            object = obj;
        }

        void dispose() {
            if (object instanceof Disposable) {
                Disposable disposableObj = (Disposable) object;
                try {
                    disposableObj.dispose();
                } catch (Throwable t) {
                    log.error("failed disposing " + object, t); //$NON-NLS-1$
                }

            }
            log.info("disposed " + object); //$NON-NLS-1$
        }
    }
}