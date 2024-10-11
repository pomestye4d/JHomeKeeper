#!/bin/bash
### BEGIN INIT INFO
# Provides:          homeKeeper
# Required-Start:    $local_fs $network $named $time $syslog
# Required-Stop:     $local_fs $network $named $time $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       Home keepers startup script
### END INIT INFO


start() {
   ${rootDir}/home-keeper-startup.sh
}

stop() {
   ${rootDir}/home-keeper-shutdown.sh
}

case "$1" in
    start)
       start
       ;;
    stop)
       stop
       ;;
    restart)
       stop
       start
       ;;
    *)
       echo "Usage: $0 {start|stop|restart}"
esac

exit 0