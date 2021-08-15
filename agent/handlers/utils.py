from datetime import datetime


# Log function
def log(msg, time=None):
    if time == None:
        time = datetime.now()
    print(time.strftime("%Y-%m-%d %H:%M:%S.%f") + ": " + msg)

# Check if string is number (float)
def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

# Check if string is int
def is_int(s):
    try:
        int(s)
        return True
    except ValueError:
        return False
