# floodgate

## Overview
Floodgate is an application designed to control the initiation, and monitoring of progress for the reindexing of content from the various content sources the Content API relies upon within the Guardian.

Floodgate is designed to be self service so that when a new content editor, and consequently, a new content source is created, the client can register themselves to Floodgate for reindexing by providing us with specific details that abide to a contract. 
 
## To run locally

```
$ ./scripts/setup.sh
$ ./scripts/start.sh
```

Note: After the first time of running, if no dependencies have been added to package.json you may simply do:

```
$ ./scripts/start.sh
```

## To run the tests

To run the tests you will need to do the following:
```
$ sbt test
```