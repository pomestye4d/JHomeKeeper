/*****************************************************************
 * Gridnine AB http://www.gridnine.com
 * Project: TorDi
 *****************************************************************/

package ru.vga.hk.core.api.rest;

import ru.vga.hk.core.api.common.BaseEvent;

public class RestEvent extends BaseEvent {
    private String requestPath;

    private final RestCallback callback;

    public RestEvent(RestCallback callbacke) {
        this.callback = callbacke;
    }

    public RestCallback getCallback() {
        return callback;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
}
