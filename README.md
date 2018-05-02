# json-validator-service

*Check whether or not your JSON complies to a schema via REST!*

json-validator-service is a service that provides three HTTP endpoints:
```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```
## Installing

Plain and simple: Clone the repository to your machine and _sbt run_ it!

## Configuring and Running

There is a .conf file at _src/main/resources/application.conf_. By default, it is set to listen to the interface 0.0.0.0 using the port 9000. Note that you might need root privilegies if you choose to listen to ports under 1025.

Another important configuration is _schemas.storageDirectory_. For now, json-validator-service simply stores the schemas in the file system. This configuration points to the directory where the schemas should be kept. Check **TO-DOs** for future perspectives of schema storage.

For now, running _sbt run_ in its own screen session should be a better call to keep track of its output and avoid problems with sighup if it is running in a remote machine. Do not forget to increase _akka.loglevel_ when running on production. 

## TO-DOs:

* Add tests! As I was mostly starting to get along with Scala and Akka, I neglected this section
* Create a service to keep it running without an attached terminal
* Test some alternatives to schema storage (e.g., redis for cache, PSQL for storage...)
