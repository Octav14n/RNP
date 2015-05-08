__author__ = 'octavian'

import sys, socket

SERVER = ('localhost', 8080)
TESTSTRING = 'Hello!¹ Σ'
COMMANDS = {
    'LOWERCASE': TESTSTRING,
    'UPPERCASE': TESTSTRING,
    'REVERSE': TESTSTRING,
    'SHUTDOWN': 'password',
    'BYE': None
}

def printHelp():
    i = 0
    for (key, value) in COMMANDS:
        i += 1
        print('[' + str(i) + '] ' + key)

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print('connecting to ' + str(SERVER), file=sys.stderr)
sock.connect(SERVER)

isRunning = True

try:
    while isRunning:

finally:
    # Close the Socket.
    sock.close()