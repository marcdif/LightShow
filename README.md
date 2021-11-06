# LightShow

A project to create a show controller system using RGB LED light strips controlled by a Raspberry Pi. The Pi acts as the show controller with the Java Show Controller Agent. Then, there is a frontend React app for playing/synchronizing music, and a Java WebSocket Server to enable communication between the Pi and all clients.

## Java Show Controller Agent

The Show Controller Agent is a Java program that controls addressable [WS2812B](https://www.google.com/search?q=WS2812B) RGB LED light strips according to provided show files, using the https://github.com/jgarff/rpi_ws281x Java library. This Agent runs as a system service and listens for instructions from the Java WebSocket Server to start and stop shows.

## Frontend Interface

The Frontend Interface is a React app to allow multiple local devices to start/stop shows and listen to the show music at the same time in (near) perfect synchronization.

## Java WebSocket Server

The WebSocket Server is a Java WSS using Netty to facilitate communication between the Java Show Controller Agent and multiple Frontend Interfaces.
