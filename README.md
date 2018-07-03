#Cassandra Performance

This tool is able to test the performance of any Cassandra-Cluster

##How it works

First you need to define a JSON-File for creating loaddata. An example you can find in the resource directory.
Following options are read by the LoadGenerator:
- Name of the Table to be tested
- List of all columns 
- List of the parts of the PartitionKey
- List of secondary indizes
- List of indizes
- Number of Queries to be generated (Selects, Inserts, Updates and Deletes)

With this information, the LoadGenerator can generate random CRUD-Queries. The queries are written in another JSON-File called "load.json".

Now start CassandraPerformance with the load.json file as argument and wait for the results ;)

##Components

###LoadGenerator

###CassandraPerformanceTester
