from handlers.actions import *
from handlers.color import Color
from datetime import datetime
import time

def log(msg, time=None):
    if time == None:
        time = datetime.now()
    print(time.strftime("%Y-%m-%d %H:%M:%S.%f") + ": " + msg)

offsets=[]

show = "test"
log("Loading actions for " + show + " show...")

firstaction = FullLight(3, Color(15, 155, 0))
size = 1

while size < 100:
    nextaction = FullLight(3, Color(30, 0, 155))

    a = firstaction
    while a.nextact != None and a.time <= nextaction.time:
        a = a.nextact

    temp = a.nextact
    a.setnextact(nextaction)
    nextaction.setnextact(temp)
    size = size + 1

log("Finished loading " + str(size) + " actions... starting " + show + " show")

starttime = time.time()

nextaction = firstaction

while True:
    timediff = time.time() - starttime
    if nextaction == None:
        break
    if timediff < nextaction.time:
        diff = nextaction.time - timediff
        if diff > 0.1:
            sleep = abs(diff-0.05)
            log("Sleeping for " + str(sleep) + " seconds...")
            time.sleep(sleep)
        else:
            log("Checking...")
        continue
    offset = nextaction.time - timediff
    log("Running " + nextaction.tostr() + ", offset=" + str(offset))
    offsets.append(offset)
    nextaction = nextaction.nextact

log(show + " has finished! " + str(time.time() - starttime))
log("Printing offsets in 2 seconds...")
time.sleep(2)

i=1
total=0
for o in offsets:
    print("Action " + str(i) + ": " + str(o))
    total = total + o
    i=i+1

avg=total/size
print("Average: " + str(avg))
