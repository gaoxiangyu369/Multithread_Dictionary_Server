# A Simple Multithread Dictionary Server

### Description

This project is to use a client-server architecture, design and implement a multi-threaded server that allows concurrent clients to search the meaning(s) of a word, add a new word, and remove an existing word.

By explicit, sockets and threads are the lowest level of abstraction for network communication and concurrency.


### Architecture

* The system will follow a client-server architecture in which multiple clients can connect to a (single) multi-threaded server and perform operations concurrently.

* The multi-threaded server implements a thread-per-connection architecture.

### Interaction

* All communication will take place via sockets based on TCP.

* The dictionary entry formats are implemented in JSON.

### Functional Requirements

* Query the meaning(s) of a given word

* Add a new word

* Remove an existing word

### Run .jar

All the files including the client, the server and the dictionary entry must be the same path.

To run the GUI, these commands must be typed in terminal:


```
java -jar Server.jar -p <port> -f <dictionary-file>

java -jar Client.jar -p <port> -h <host>
```
