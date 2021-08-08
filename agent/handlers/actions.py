class ShowAction:
    def __init__(self, time, nextact=None):
        self.time = time
        self.nextact = nextact
        pass

    def tostr(self=None):
        return self.actiontype() + "-" + str(self.time)

    def actiontype(self=None):
        raise NotImplementedError("Cannot call this function on ShowAction class")

    def nextact(self):
        return self.nextact

    def setnextact(self, nextact):
        self.nextact = nextact

    def time(self):
        return self.time

class FullLight(ShowAction):
    def __init__(self, time, color):
        super().__init__(time)
        self.color = color

    def actiontype(self=None):
        return 'FullLight'

    def color(self):
        return self.color
