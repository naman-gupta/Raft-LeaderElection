#CS451-2015 Distributed System Project

##Leader Election using Raft Consensus Protocol.

###Introduction

Raft is a consensus algorithm for electing a leader in a distributed environment and managing a replicated log. It produces a result equivalent to (multi-)Paxos, and it is as efficient as Paxos, but its structure is different from Paxos; this makes Raft more understandable than existing and well known Paxos and also provides a better foundation for building practical systems. In order to enhance understandability, Raft separates the key elements of consensus, such as leader election, log replication, and safety. Here our main focus is on electing a unique leader in a distributed environment, while ensuring safety and liveness.

###Implementation

Can be found in Report.pdf

###Contact
Naman Gupta : naman.bbps[at]gmail.com
