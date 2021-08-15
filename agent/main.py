from handlers.utils import *
from handlers.actions import *
from handlers.color import Color
import time

try: 
    Debug = False
    DebugV = False
    TempValues = False
    show = "shows/gny"

    ShowName = "Unknown"
    ShowAudio = "Unknown"
    ShowTime = "Seconds"
    firstaction = None
    size = 0

    # Whether to load temporary values in the show (skips reading from file)
    if TempValues:
        log("Filling in temporary actions for " + show + " show...")
        firstaction = FullLight(0, Color(255, 255, 255))
        size = size + 1

        howmany = 1000

        while size < howmany:
            # nextaction = Log(3 + (size * 0.5), "Action " + str(size + 1))
            # nextaction = Log(3 + (size * 0.25 * 4), ".", size + 1)
            # if size % 2 == 0:
            #     nextaction = FullLight(0 + (size * (1/100)), Color(255, 255, 255))
            # else:
            #     nextaction = FullLight(0 + (size * (1/100)), Color(0, 0, 0))
            nextaction = FullLight(0 + (size * (1/howmany)), Color(255 - (255 * (size / howmany)), 255 - (255 * (size / howmany)), 255))

            a = firstaction
            while a.nextact != None and a.time < nextaction.time:
                a = a.nextact

            temp = a.nextact
            a.setnextact(nextaction)
            nextaction.setnextact(temp)
            size = size + 1
    else:
        # Read from file
        log("Loading actions for " + show + " show...")
        with open(show + ".show") as f:
            lines = f.readlines()
        lines = [x.rstrip() for x in lines]

        if Debug: print("Printing action parent assignments:")

        # Values used to create multi-line for loops
        for_loop = None
        for_loop_firstaction = None

        for line in lines:
            # Tokens must be tab-separated - 4 spaces does not count as a tab!
            tokens = line.split("\t")
            nextaction = None

            if for_loop != None and tokens[0] == '':
                tokens.remove('')
            first = tokens[0]

            if first.startswith("#"):
                continue
            elif first == 'Show':
                act = tokens[1]
                if act == 'Name':
                    ShowName = tokens[2]
                elif act == 'Audio':
                    ShowAudio = tokens[2]
                elif act == 'Time':
                    ShowTime = tokens[2]
                elif act == 'Debug':
                    Debug = tokens[2] == 'true'
            elif is_number(first):
                    t = float(first)
                    act = tokens[1]
                    if act == 'Log':
                        nextaction = Log(t, tokens[2])
                    elif act == 'For':
                        if tokens[4] != '{':
                            raise SyntaxError("Missing { in 'For' action!")
                        for_loop = For(t, int(tokens[2]), float(tokens[3]))
                        continue
                    elif act == 'FullLight':
                        color = Color(int(tokens[2]), int(tokens[3]), int(tokens[4]))
                        nextaction = FullLight(t, color)
            elif first == '}': # End of a For loop
                if for_loop_firstaction == None:
                    raise SyntaxError("For loop must have at least 1 action!")
                for_loop.setfirstaction(for_loop_firstaction)
                nextaction = for_loop
                for_loop = None
                for_loop_firstaction = None
            
            if nextaction != None or (for_loop != None and for_loop_firstaction == None):
                if for_loop != None:
                    rel_firstaction = for_loop_firstaction # For loop
                else:
                    rel_firstaction = firstaction # Main Show loop
                    
                if rel_firstaction == None:
                    if for_loop != None:
                        for_loop_firstaction = nextaction # For loop
                    else:
                        firstaction = nextaction # Main Show loop
                else:
                    if Debug: print("  Processing " + nextaction.tostr())
                    # Sort as we go
                    a = rel_firstaction
                    last = None
                    while a.nextact != None and a.time < nextaction.time:
                        last = a
                        a = a.nextact
                        if Debug: print("    At " + a.tostr())
                        if a.time > nextaction.time:
                            a = last
                            if Debug: print("      Too far... jumping back to " + a.tostr())
                            break

                    if Debug: print("      Setting parent to " + a.tostr())

                    # Insert our new action (nextaction) in between 'a' and its 'nextact' (which may or may not be None)
                    temp = a.nextact
                    a.setnextact(nextaction)
                    nextaction.setnextact(temp)
                size = size + 1

    log("Finished loading " + str(size) + " actions... starting " + show + " show")

    starttime = time.time()

    RunningActions = []

    nextaction = firstaction

    lastrun = starttime
    count=0

    while True:
        currenttime = time.time()
        nextrun = starttime + ((count + 1) * 0.01)
        # Only run every 0.01 seconds (100 times per second)
        if currenttime < nextrun:
            if DebugV: print("Sleep for " + str((lastrun + 0.01) - time.time()))
            time.sleep(nextrun - time.time())
            continue

        lastrun = currenttime
        count = count + 1
        # print("Run " + str(count))

        # Calculate number of seconds we are into the show
        timediff = currenttime - starttime

        # Array for storing any actions that are done
        done = []
        # Process all running actions
        for act in RunningActions:
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
            RunningActions.remove(act)
        # if Debug: print("RunningActions: " + str(len(RunningActions)) + ", done: " + str(len(done)))
        done.clear()

        if nextaction == None:
            # Stop show if there are no actions left
            if len(RunningActions) == 0:
                break
            # Otherwise, just skip this while pass
            continue

        # 1) If it's time for the next action to start...
        while nextaction != None and timediff >= nextaction.time:
            # 2) add it to the list of RunningActions...
            RunningActions.append(nextaction)
            # 3) and run it for the first time...
            nextaction.run()
            # 4) and update nextaction to the next action.
            nextaction = nextaction.nextact
            # 5) Continue looping until the next action shouldn't start yet.
            continue

        # Continue on to the next main loop.
        continue
    
    set_full_strip(Color(0, 0, 0))
    log(show + " has finished! " + str(time.time() - starttime))

except KeyboardInterrupt:
    log("Exiting...")
    set_full_strip(Color(0, 0, 0))
