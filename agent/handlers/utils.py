from handlers.color import Color
from datetime import datetime


# Log function
def log(msg, time=None):
    if time == None:
        time = datetime.now()
    print(time.strftime("%Y-%m-%d %H:%M:%S.%f") + ": " + msg, flush=True)

# Check if string is number (float)
def is_number(s: str):
    try:
        float(s)
        return True
    except ValueError:
        return False

# Check if string is int
def is_int(s: str):
    try:
        int(s)
        return True
    except ValueError:
        return False

def get_color(text: str) -> Color:
    if text.__contains__(','):
        rgb = text.split(',')
        r = int(rgb[0])
        g = int(rgb[1])
        b = int(rgb[2])
        return Color(r, g, b)
    else:
        return Color(text)

def color_delta(final: Color, initial: Color) -> Color:
    return Color(final.r - initial.r, final.g - initial.g, final.b - initial.b)

def color_add(color: Color, amount: int) -> Color:
    return Color(color.r + amount, color.g + amount, color.b + amount)

def color_add(color: Color, amount: Color) -> Color:
    return Color(color.r + amount.r, color.g + amount.g, color.b + amount.b)

def color_mult(color: Color, amount: float) -> Color:
    return Color(round(color.r * amount), round(color.g * amount), round(color.b * amount))
