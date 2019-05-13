# Simple chat server that supports multiple clients.

## Building the application

To build the application build tool [Maven](https://maven.apache.org/) is needed. 

To build an executable jar file, enter the following: `mvn clean compile package`. 
After build process is complete resulting jar file will be placed in _target_ directory.

## Running the application

This application requires Java runtime. 
To run the application, enter the following: `java -jar <path-to-jar>`. 
Where _&lt;path-to-jar&gt;_ is an absolute or a relative path to the applications jar file. 

### Protocol for communicating with the server is as follows:

1. Establish a TCP connection with the server.
2. Send a UTF-8 encoded message that represents a clients username.
3. All further sent UTF-8 encoded messages after the first one will be interpreted as messages from provided username.

