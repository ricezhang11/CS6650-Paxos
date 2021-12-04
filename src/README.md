How to run the program?

1. open two consoles, one for the client, the other for the server.
2. in the server console, run /bin/bash deploy.sh
3. in the client console, run /bin/bash run_client.sh myclientcontainer
4. then you should see in the client console, 15 operations to be executed including 5 PUT operations, 5 GET operations and 5 DELETE operations.
Note that to avoid these initial requests to overlap with each other, thread will sleep for 15 second before sending out the next request to
account for the acceptor failures. Therefore, the initial requests take some time. 
5. after these initial requests are complete, you'll see "Please enter a valid operation below:" and some examples of valid operations.
6. Now you can enter valid commands like "PUT 4 56" or invalid commands like "PUT" and see the server's responses 

Note:
1. Threads on the server sides are deliberately slowing down to show the effect of Paxos algorithm. If you want to disable that, please go to the class
AcceptorImpl.java --> go to method "prepare" and delete line 123 "Thread.sleep(1000);"
2. According to homework requirement, we should configure acceptors to fail randomly and this is currently set in the code. If you want to disable that,
 please go to class DataStoreServer.java --> go to the main method and comment out line 61. 
3. Also, all three kinds of requests "PUT", "DELETE", "GET" are involved in the Paxos process. I didn't differentiate them, but it's straight-forward
to exclude "GET" requests from Paxos if we want. 

For the executive summary (assignment overview, technical impression and further enhancement), please find Executive_Summary.pdf under src directory
For snapshots and code result walk-through (a more detailed explanation of how Paxos works in the test cases). Please find Project4code walkthrough.pdf under src/snapshots directory.
