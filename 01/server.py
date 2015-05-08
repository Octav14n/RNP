
__author__ = 'octavian'


import sys
import socket
import threading
import traceback
import errno

DEBUG = False # Quits Server after one connection
HOST = 'localhost'
PORT = 50000
BYE = 'BYE'
ENCODE = 'utf-8'
PASSWORD = 'password'
NL = '\n'
MAXCONNECTIONS = 3

ARG_EXC = 'The format of the argument is not valid.'

sock = socket.socket
# If set to False no new connections will be accepted. Open connections will still be served.
keepRunning = True
# A list of all clients. (Type: Client)
saccept = threading.Thread()
clients = []

def init():
    global sock
    # Create and bind a TCP/IP Socket to the configured port.
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # REUSEADDR so the TCP-Port will be reused instead of throwing an error.
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((HOST, PORT))


def main():
    global keepRunning, saccept
    # Virtual Main
    assert sock is not None and sock is not False, 'Socket is not established.'
    sock.listen(0)
    print('Server started.')

    while keepRunning or len(clients) > 0:
        if keepRunning and len(clients) < MAXCONNECTIONS:
            if saccept.is_alive():
                saccept = threading.Thread(target=client_accept)
                saccept.setDaemon(True)
                saccept.start()

        # Getting Input of all clients (one client per thread)
        threads = []
        for client in clients:
            t = threading.Thread(target=client_update, args={client})
            t.setDaemon(True)
            t.start()
            threads.append(t)

        # Waiting for all clients to finish
        for thread in threads:
            thread.join()

def client_accept():
    global keepRunning
    # connection is the socket to communicate with the client.
    # client_address is a tuple with ip and client-port.
    connection, client_address = sock.accept()
    print('Connection from ', client_address)
    client = Client(connection, client_address)
    clients.append(client)

    if DEBUG:
        # In Debug-Mode the server will only accept one connection and exit afterward.
        keepRunning = False

def client_update(client):
    assert isinstance(client, Client), 'The Client is not valid.'
    client.update()

# Server commands
def srv_lowercase(client, argument):
    assert isinstance(argument, str), ARG_EXC
    return argument.lower()

def srv_uppercase(client, argument):
    assert isinstance(argument, str), ARG_EXC
    return argument.upper()

def srv_reverse(client, argument):
    assert isinstance(argument, str), ARG_EXC
    return argument[::-1]

def srv_bye(client, argument):
    client.bye()
    return None

def srv_shutdown(client, argument):
    global keepRunning
    assert isinstance(argument, str), ARG_EXC
    assert argument == PASSWORD, 'The password is not valid.'
    keepRunning = False
    return None


class Client:
    def __init__(self, connection, identifier):
        # connection is the connection (Socket) to a client.
        # identifier is a helper to identify which client sends a Debug message.
        assert isinstance(connection, socket.SocketType), 'The connection is not a Socket.'
        self.connection = connection
        self.id = identifier

    def update(self):
        try:
            self._update()
        except ConnectionResetError as eCRE:
            # If the connection was not terminated with a "BYE" and corresponding "OK BYE".
            print(str(self) + 'Connection was gracefully reset.')
            self.destroy()
        except Exception as eExc:
            try:
                # Try to inform the Client of the Problem
                print(str(self) + 'Handled Exception:', file=sys.stderr)
                traceback.print_exc(file=sys.stderr)
                print('Informing client.', file=sys.stderr)
                self.err(str(eExc))
            except Exception as e2:
                # Everything went bad. The client will be terminated. The Server'll be back.
                print(str(self) + 'Handled Exception:', file=sys.stderr)
                traceback.print_exc(file=sys.stderr)
                print(str(self) + 'Destroying client.', file=sys.stderr)
                self.destroy()

    def _update(self):
        # Retrieving message
        data = self.connection.recv(255)
        assert isinstance(data, bytes), 'The data format is not valid.'
        line = data.decode(ENCODE)
        # Each message has to end with \n and can only have one \n.
        assert line.find(NL) == (len(line) - 1), 'The first NewLine is not the last character.'

        # Remove NL
        line = line[:-1]
        # Get the command and the arguments if available.
        if line.find(' ') == -1:
            command = line
            argument = None
        else:
            command, argument = line.split(' ', 1)
        # Ensure that the command is valid.
        assert command in COMMANDS, 'The command was not found.'

        # Execute the command.
        response = COMMANDS[command](self, argument)
        if response is not None:
            self.send(response)

    def err(self, msg):
        self.send(msg, 'ERROR')

    def send(self, msg, pre='OK'):
        print("Sending: " + str(msg) + " pre="+pre)
        assert isinstance(msg, str), 'The message to send is not valid.'
        toSend = (pre + ' ' + msg).replace('\n', '') + NL
        try:
            self.connection.send(toSend.encode(ENCODE))
        except BrokenPipeError as e:
            if e.errno == errno.EPIPE:
                print(str(self) + 'Could not send because of a Disconnect: "' + toSend + '"')
                self.destroy()
            else:
                pass

    def bye(self):
        self.send(BYE)
        self.destroy()

    def destroy(self):
        print('Destroying ' + self.id)
        self.connection.close()
        clients.remove(self)

    def __str__(self):
        return 'Client<' + str(self.id) + '>'


COMMANDS = {
    'LOWERCASE': srv_lowercase,
    'UPPERCASE': srv_uppercase,
    'REVERSE': srv_reverse,
    'SHUTDOWN': srv_shutdown,
    'BYE': srv_bye
}

# Actual Main
try:
    init()
    main()
except Exception as e:
    sock.close()
    print(str(e), file=sys.stderr)