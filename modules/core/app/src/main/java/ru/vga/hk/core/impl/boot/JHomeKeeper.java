package ru.vga.hk.core.impl.boot;

import ru.vga.hk.core.api.boot.Activator;
import ru.vga.hk.core.api.environment.Environment;

import java.util.Comparator;
import java.util.ServiceLoader;

public class JHomeKeeper {
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(Environment::dispose));
        var services = ServiceLoader.load(Activator.class).stream()
                .map(ServiceLoader.Provider::get).sorted(Comparator.comparing(Activator::getPriority))
                .toList();
        for(var service: services){
            service.configure();
        }
        for(var service: services){
            service.activate();
        }
        System.in.read();
    }
}
