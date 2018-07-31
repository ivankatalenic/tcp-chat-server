# tcp-server

## Simple TCP-based server for UTF-8 encoded text messages that supports multiple clients.

### Protocol for communicating with the server is as follows:
1. Establish a TCP connection with the server.
2. Send a UTF-8 encoded message that represents a client username.
3. All further sent UTF-8 encoded messages after the first one will be interpreted as messages from username.
