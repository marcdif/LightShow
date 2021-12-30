# LightShow

A project to create a show controller system using RGB LED light strips controlled by a Raspberry Pi. The Pi acts as the show controller, with a frontend React app for playing/synchronizing music, and a Java WebSocket Server to enable communication between the Pi and all clients.

## Show Controller Daemon

The Show Controller Daemon is a Java program that controls addressable [WS2812B](https://www.amazon.com/dp/B01CDTECSG) RGB LED light strips according to provided show files, using the https://github.com/jgarff/rpi_ws281x Java library.

## Frontend Interface

The Frontend Interface is a React app to allow multiple local devices to listen to the music of the show at the same time.

## Sync Server

The Sync Server is a Java WSS using Netty to facilitate communication between the Show Controller Daemon and multiple Frontend Interfaces.
