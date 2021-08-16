import webcolors

class Color:
    def __init__(self, name: str):
        if name.startswith('#'):
            array = webcolors.hex_to_rgb(name)
        else:
            array = webcolors.name_to_rgb(name)
        self.__init__(array[0], array[1], array[2])

    def __init__(self, r: int, g: int, b: int):
        self.r = r
        self.g = g
        self.b = b
        if r > 255 or r < 0:
            raise ValueError('Color value (red) must be in the range [0-255]')
        if g > 255 or g < 0:
            raise ValueError('Color value (green) must be in the range [0-255]')
        if b > 255 or b < 0:
            raise ValueError('Color value (blue) must be in the range [0-255]')
    
    def r(self):
        return self.r
    
    def g(self):
        return self.g
    
    def b(self):
        return self.b
    
    def hex(self):
        return '#%02x%02x%02x' % (self.r, self.g, self.b)

    def tostr(self):
        return str(self.r) + "," + str(self.g) + "," + str(self.b)
