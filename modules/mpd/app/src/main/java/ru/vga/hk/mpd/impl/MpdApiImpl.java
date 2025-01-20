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

package ru.vga.hk.mpd.impl;

import org.bff.javampd.server.MPD;
import ru.vga.hk.mpd.api.MpdApi;

public class MpdApiImpl implements MpdApi {
    private String ip;
    private String login;
    private String password;

    public MpdApiImpl(String ip) {
        this.ip = ip;
        this.login = null;
        this.password = null;
    }

    public MpdApiImpl(String ip, String login, String password) {
        this.ip = ip;
        this.login = login;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void stop() {
        var mpd = MPD.builder()
                .server(ip)
                .port(6600)
                .build();
        try{
            mpd.getPlayer().stop();
        } finally {
            mpd.close();
        }
    }

    @Override
    public void play() {
        var mpd = MPD.builder()
                .server(ip)
                .port(6600)
                .build();
        try{
            mpd.getPlayer().play();
        } finally {
            mpd.close();
        }


    }
}
