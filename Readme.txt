This folder contains programs related to AOS project 2

This folder contains a MAINCLIENT code and MAINSERVER code..
The main server waits for the clients..
While the mainclient sends requests to connect to the servers..

Run the mainserver program first and then run the mainclient program.
The program is designed in such a way that the first client sends a message to 
all the other clients to begin execution.

When the clients begin execution, they start sending requests to access the 
critical section.
Permissions are given based on the Maekawa algorithm.

When the execution is completed, server 1 sends DONE message to all the clients.