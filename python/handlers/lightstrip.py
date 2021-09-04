try:
    rpi_led_module = __import__("rpi_ws281x")
except ImportError:
    rpi_led_module = None

_local_Debug = True

# LED strip configuration:
LED_COUNT = 150        # Number of LED pixels.
LED_PIN = 18          # GPIO pin connected to the pixels (18 uses PWM!).
# LED_PIN = 10        # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_FREQ_HZ = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA = 10          # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255  # Set to 0 for darkest and 255 for brightest
LED_INVERT = False    # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL = 0       # set to '1' for GPIOs 13, 19, 41, 45 or 53

if rpi_led_module != None:
    strip = rpi_led_module.PixelStrip(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
    strip.begin()

def _local_log(text):
    if _local_Debug: print("[*] " + text)

def _to_native_color(color):
    if rpi_led_module != None:
        return rpi_led_module.Color(color.r, color.g, color.b)
    else:
        return color

def set_full_strip(color):
    _local_log("Set full strip to " + color.tostr())
    if rpi_led_module != None:
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, _to_native_color(color))
        strip.show()

def set_single_led(id, color):
    _local_log("Set LED #" + str(id) + " to " + color.tostr())
    if rpi_led_module != None:
        strip.setPixelColor(id, _to_native_color(color))
        strip.show()
