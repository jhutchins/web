#Intro
This is a simple file based web server. It supports GET requests to retrieve files stored at a configured location.

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
