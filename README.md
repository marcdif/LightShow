# LightShow

A project to create a show controller system using RGB LED light strips controlled by a Raspberry Pi. The Pi acts as the show controller, with a frontend React app for playing/synchronizing music, and a Java WebSocket Server to enable communication between the Pi and all clients.

## Show Controller Agent

The Show Controller Agent is a Python program that controls addressable [WS2812B]() RGB LED light strips according to provided show files, using the https://github.com/jgarff/rpi_ws281x Python library. Currently, the program runs one show and ends; ultimately it will run as a service with the ability to run multiple shows without stopping.

## Frontend Interface

The Frontend Interface is a React app to allow multiple local devices to listen to the music of the show at the same time.

## Java WebSocket Server

The WebSocket Server is a Java WSS using Netty to facilitate communication between the Show Controller Agent and multiple Frontend Interfaces.
