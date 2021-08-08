class Color:
    def __init__(self, r, g, b):
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
