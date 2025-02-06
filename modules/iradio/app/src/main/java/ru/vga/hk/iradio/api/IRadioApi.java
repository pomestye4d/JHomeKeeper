package ru.vga.hk.iradio.api;

public interface IRadioApi {
    void stop();
    void play(int radioIdx);
    void increaseVolume();
    void decreaseVolume();
    void nextSong();
}
