# LightShow

A project to create a show controller system using RGB LED light strips controlled by a Raspberry Pi. The Pi acts as the show controller with the Java LED Agent. Then, there is a React Frontend Interface for playing/synchronizing music, and a Java "Sync Server" to enable communication between the Pi and all clients.

## LED Agent

The LED Agent is a Java program that controls addressable [WS2812B](https://www.google.com/search?q=WS2812B) RGB LED light strips according to provided show files, using the https://github.com/jgarff/rpi_ws281x Java library. This Agent runs as a system service and listens for instructions from the Sync Server to start and stop shows.

## Frontend Interface

The Frontend Interface is a React app to allow multiple local devices to start/stop shows and listen to the show music at the same time in (near) perfect synchronization.

## Sync Server

The Sync Server is a Java WSS using Netty to facilitate communication between the Show Controller Daemon and multiple Frontend Interfaces.

## Notes

- Raspberry PIs don't have a built-in RTC clock, which computers use to keep track of time that passes when the computer isn't running. The PI uses the `systemd-timesyncd` service to sync with time servers, but this doesn't finish running until 20-30 seconds after startup (NTP calculations can take some time). To compensate, the systemd service file I used for the LEDAgent (included below) specifies `After = time-sync.target` and `Wants = time-sync.target`. This means the `LEDAgent.service` won't run until `systemd-time-wait-sync` finishes running, which is a service specifically built to only run once `systemd-timesyncd` finishes. Yes, it's complicated.
    - Without waiting for `systemd-timesyncd`, the LEDAgent will start up and get a certain `syncServerTimeOffset` value (mine was consistently around -10000ms). Then, when `systemd-timesyncd` finishes running, the clock will change, meaning the previous `syncServerTimeOffset` is now wrong.
    - The only solution for this is to either (a) restart `LEDAgent.service` after the clock is accurate, or (b) wait to start `LEDAgent.service` until the clock is accurate.

# LEDAgent systemd configuration

## LEDAgent.service file (/etc/systemd/system/LEDAgent.service)

```
[Unit]
Description = Java LEDAgent
After = network.target time-sync.target
Wants = time-sync.target

[Service]
Type = forking
ExecStart = /usr/local/bin/LEDAgent.sh start
ExecStop = /usr/local/bin/LEDAgent.sh stop
ExecReload = /usr/local/bin/LEDAgent.sh reload

[Install]
WantedBy=multi-user.target
```

## LEDAgent.sh startup script (/usr/local/bin/LEDAgent.sh)

```
#!/bin/sh
SERVICE_NAME=LEDAgent
PATH_TO_JAR=/home/pi/LEDAgent.jar
PID_PATH_NAME=/tmp/LEDAgent-pid
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -jar $PATH_TO_JAR >> /var/log/LEDAgent.log 2>&1&
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java -jar $PATH_TO_JAR >> /var/log/LEDAgent.log 2>&1&
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
```
