/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: TorDi
 *****************************************************************/

package ru.vga.hk.core.api.common;

import ru.vga.hk.core.api.event.EventSource;

public class BasicEventSource<T extends BaseEvent> implements EventSource<T> {

    private final String id;

    public BasicEventSource(String id) {
        this.id = id;
    }

    @Override
    public void dispose() {
        //noops
    }

    @Override
    public String getId() {
        return id;
    }
}
