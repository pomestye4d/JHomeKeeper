#!/bin/bash
cd ${rootDir}
jre/bin/java  -cp "lib/*" -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n ${timezone} -Dhome-keeper-tag=true ru.vga.hk.core.impl.boot.JHomeKeeper >/dev/null 2>&1 &