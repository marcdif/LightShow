import time
from handlers.lightstrip import *

class ShowAction:
    def __init__(self, time, nextact=None):
        self.time = time
        self.nextact = nextact
        pass

    def tostr(self=None):
        return self.actiontype() + "-" + str(self.time)

    def actiontype(self=None):
        raise NotImplementedError("Cannot call this function on ShowAction class")

    # Return true if the action is finished, and doesn't need to run anymore
    def run(self=None):
        raise NotImplementedError("Cannot call this function on ShowAction class")
    
    def finished(self):
        return

    def nextact(self):
        return self.nextact

    def setnextact(self, nextact):
        self.nextact = nextact

    def time(self):
        return self.time

class Log(ShowAction):
    count = 0
    # number of seconds
    interval = 0.5 * 4
    lastrun = 0
    def __init__(self, time, text, num=1):
        super().__init__(time)
        self.text = str(text)
        self.num = num
        self.done = False
    
    def actiontype(self=None):
        return 'Log'
    
    def run(self):
        # Don't run if we're already done
        if self.done == True:
            return

        # Skip running if the interval time hasn't passed yet
        if time.time() <= (self.lastrun + self.interval) and self.lastrun != 0:
            # print("Skipping " + self.text + "..." + str(time.time()) + "|" + str(self.interval))
            return

        # Print the text
        print("Text: ", end='')
        for i in range(self.num):
            print(self.text, end='')

        print("\n", end='')

        # Update the number of times we've run
        self.count = self.count + 1
        # If we've run 1 times, we're done
        if self.count >= 1:
            self.done = True

        # Update the last time we've run
        self.lastrun = time.time()
    
    def isdone(self):
        return self.done
    
    def text(self):
        return self.text

    def tostr(self=None):
        return self.actiontype() + "-" + str(self.time) + "-" + self.text

class For(ShowAction):
    def __init__(self, t, count, delay):
        super().__init__(t)
        self.count = count
        self.delay = delay
        self.done = False

        self.Debug = False

        self.initialstarttime = 0
        self.starttime = 0
        self.i = 1
        self.nextaction = None
        self.RunningActions = []
    
    def setfirstaction(self, firstaction):
        if self.Debug: print("ForLoop First action set to " + firstaction.tostr())
        self.firstaction = firstaction
        self.nextaction = self.firstaction

    def actiontype(self=None):
        return 'For'

    def run(self):
        if self.initialstarttime == 0:
            if self.Debug: print("Beginning of FIRST For loop...")
            self.initialstarttime = time.time()
            self.starttime = time.time()
        currenttime = time.time()
        # If it's time to start the loop from the beginning...
        if currenttime >= (self.initialstarttime + (self.i * self.delay)):
            self.i = self.i + 1
            self.starttime = time.time()
            if self.i >= self.count:
                self.done = True
                return
            if self.Debug: print("Beginning of For loop...")
            self.nextaction = self.firstaction
            self.RunningActions.clear()

        # Calculate number of seconds we are into the for loop
        timediff = currenttime - self.starttime

        # Array for storing any actions that are done
        done = []
        # Process all running actions
        for act in self.RunningActions:
            # 1) If the action is done, add to the 'to remove' list...
            if act.isdone():
                done.append(act)
            else:
                # 2) otherwise run it.
                # Note: Actions are responsible for tracking the interval they're supposed to run at.
                # act.run() could be called 100 times per second.
                # If the action is only meant to run 10 times per second, it should have a counter variable.
                act.run()
                if act.isdone():
                    done.append(act)

        # Remove all done actions from RunningActions
        for act in done:
            self.RunningActions.remove(act)
        
        if self.nextaction == None:
            # Stop action if there are no actions left
            if len(self.RunningActions) == 0:
                # self.i = self.i + 1
                # self.starttime = time.time()
                # if self.i >= self.count:
                #     self.done = True
                # self.nextaction = self.firstaction
                # self.RunningActions.clear()
                return
            # Otherwise, just skip this pass
            return

        # 1) If it's time for the next action to start...
        while self.nextaction != None and timediff >= self.nextaction.time:
            # 2) add it to the list of RunningActions...
            self.RunningActions.append(self.nextaction)
            # 3) and run it for the first time...
            self.nextaction.run()
            # 4) and update self.nextaction to the next action.
            self.nextaction = self.nextaction.nextact
            # 5) Continue looping until the next action shouldn't start yet.
            continue
    
    def isdone(self):
        return self.done

    def tostr(self=None):
        return self.actiontype() + "-" + str(self.time)

class FullLight(ShowAction):
    def __init__(self, time, color):
        super().__init__(time)
        self.color = color
        self.done = False

    def actiontype(self=None):
        return 'FullLight'

    def color(self):
        return self.color
    
    def run(self):
        set_full_strip(self.color)
        self.done = True
    
    def isdone(self):
        return self.done

    def tostr(self=None):
        return self.actiontype() + "-" + str(self.time) + "-" + self.color.tostr()
