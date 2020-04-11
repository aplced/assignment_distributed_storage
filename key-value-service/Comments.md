In resources\application.properties if the setting dbtest.initialize is set to true and the database is empty it will be populated with 1 million entries on startup for testing purposes.

### Tasks 2

Querying a million of entries persistent on mongodb takes ~7 seconds, which considering the text format of the data is ok-ish. 
One way to aleviate the size of the data and still be considered a REST-ful api is to implement pagination and enable the client to fetch the data in ideally configurable chunks.
If performace is important though maybe other steps can be taken - compress the data, chose a non textual format, have the host stream the data to the client. It really depends
on the context of the solution though.

### Task 3

docker-compose.yaml in project root starts up the application

### Task 4

The described problem is not very trivial and is being solved by existing products like Cassandra (and looking at cockroach db also, but I haven't used it) and if I had the option I'd opt to use a cassandra cluster of nodes to achieve the desired effect.
Since I am a bit "tainted" by using Cassandra if I had to do an implementation I'd probably go the same route (provided I don't know usecase specifics and I am assuming eventual consistency is ok):
 - each node would be configured to connect to another node and once it reaches it through some internal API collect a lsit of the other nodes participating in the cluster.
 - each node would be recording it's data updates with a timestamp, that would be used to resolve conflicts with other nodes - i.e. newerer timestamp means newer version of the data.
 - deletions would be marked as such and not really deleted from the db in order to be able to resolve conflicts.
 - this means that writes to the db can be fast and resolved later
 - the problem of reads still remains - one could read stale data from the current node or be forced to for the node that he is accessing to talk to all the other nodes in the cluster and resolve data conflicts before returning the data.

I have pushed a branch with a naive attempt of the implementaiton albeit not using eventual consistency - all writes are forced to sync all nodes in the cluster and returning error if a fresher copy of the data exists. 