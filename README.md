#Intro
This is a simple file based web server. It supports GET requests to retrieve files stored at a configured location.

Supports `If-None-Match` and `If-Modified-Since` headers in requests
## Build
```
mvn install
```

## Run
```
java -jar target/test-1.0-SNAPSHOT.one-jar.jar --config ./dev.conf
```
The config parameter is required and the provided file needs to include the following parameters in JSON format.

|   Name   |                    Description                   |
|:--------:|:------------------------------------------------:|
| baseDir  | The base directory from which to server files    |
| port     | The port to which the web server should be bound |
| poolSize | The size of the thread pool                      |

For example
```JSON
{
    "baseDir": ".",
    "port": 8000,
    "poolSize": 100
}
```

##Performance
Included is a script for running performance testing. The results of running these tests on a MacBook Pro with a 3.1 GHz Intel Core i7 processor and 16 GB 1867 MHz DDR3 RAM are
```Bash
./test.sh
Parallel Test 100 requests for 10MB file

real	0m0.887s
user	0m0.652s
sys	    0m1.225s


Sequential Test 100 requests for 10MB file

real	0m1.855s
user	0m0.470s
sys	    0m0.838s
```
