# CSD2223 Project 1

## Introduction

This is a project for the course CSD - Dependable Distributed Systems, at University Nova de Lisboa.
The project consists in implementing a Open Good Market of auction system with a Byzantine Fault Tolerant Replicated State Machine and a Blockchain to store the transactions.
The project is implemented in Java, using the [Babel Framework](https://asc.di.fct.unl.pt/~jleitao/babel/).
The project contains 2 main folders, the client and the server. The client is used to submit operations to the server, and the server is used to run the replicas.

## Authors

-Maria Eduarda Paiva Contins, m.contins@campus.fct.unl.pt
-Rémi Bourdais, r.bourdais@campus.fct.unl.pt
-André Miguel Mangericão Cordeiro, am.cordeiro@campus.fct.unl.pt

## How to run

To run the project, you need to have Java 8 installed and Maven.
A makefile is provided to run the project, but you can also run it manually.

### Makefile commands

To run the project, you only need to run the following command:

`make nbr=<number_of_replicas>` 

This command will build the project, start the number of replica given and the client.

The client and the replicas will pop up in different terminals.

Only 4 keys are provided, so the maximum number of replicas is 4.

The replicas will be started in the following ports: 5000, 5002, 5004, 5006.

The client will be started in the port 6000, 6001, 6002, 6003.

The makefile has the following commands:

default: all 

- `make build` - builds the project

- `make clean` - cleans the project

- `make info` - shows the project running information

- `make start_client` - starts the client

- `make start_replica` - starts the replicas

- `make stop` - stops the replicas and the client

- `make kill` - kills the replicas and the client

- `make all` - builds the project, starts the replicas and the client

### Manual commands

To run the project manually, you need to run the following commands:

`mvn package` in the root folder of the replica and the client

then, in the deploy folder of the replica, run the following commands:

`java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5000 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node1`

`java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5002 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node2`

`java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5004 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node3`

`java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5006 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node4`

then, in the client deploy folder, run the following commands:

`java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-client.jar`


# Old README

To build:
    - you need Maven installed
    - execute "mvn package" in the root folder
    - a jar file will be placed in the "deploy" folder

To run:
    - in folder "deploy" execute:
        java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=<base_port> initial_membership=<membership> crypto_name=<crypto_name> submit=<submit>
    - example to run 4 replica:
        java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5000 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node1
        java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5002 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node2
        java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5004 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node3
		java -Dlog4j.configurationFile=log4j2.xml -jar csd2223-proj1.jar base_port=5006 initial_membership=localhost:5000,localhost:5002,localhost:5004,localhost:5006 crypto_name=node4

 The parameters are:
    - base_port: the base port to use for the replica, the example uses base_port and base_port+1, do not use sequential ports!
    - initial_membership: the initial membership of the group, consisting in ip:port pairs separated by commas
    - crypto_name: the name of the private key to use for the replica
    - submit: if true, the replica simulate clients and submit operations