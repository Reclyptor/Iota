# Iota
IoT Telemetry Project \
![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/Reclyptor/iota?color=blue&label=Release&sort=semver&style=plastic)
![GitHub](https://img.shields.io/github/license/Reclyptor/iota?color=red&label=License&style=plastic)
![GitHub repo size](https://img.shields.io/github/repo-size/Reclyptor/iota?color=green&label=Size&style=plastic)

## How To Run
### Build Project Image
`./gradlew :assemble` \
`docker-compose build`

### Run Application Suite
`docker-compose up`

## Notes
The [docker-compose.yml](https://github.com/Reclyptor/Iota/blob/master/docker-compose.yml) file contains various environment variables that may be adjusted to change the periodicity of the batch jobs running within the application.

Note that all interval configurations are in <b>seconds</b>.

- <b>TELEMETRY_LOGGING_INTERVAL_READ</b>: telemetry polling interval
- <b>TELEMETRY_LOGGING_INTERVAL_WRITE</b>: telemetry logging interval
- <b>TELEMETRY_REPORTING_INTERVAL_WRITE</b>: telemetry reporting interval
- <b>TELEMETRY_REPORTING_THRESHOLD_TEMPERATURE</b>: reports will only include entries whose temperature matches or exceeds this value

These environment variables are completely optional. If not provided, default values will be used by the application instead. These default values - as well as additional configurable environment variables - can be found in [application.properties](https://github.com/Reclyptor/Iota/blob/master/src/main/resources/application.properties).

## Web Endpoints
Assuming that the application is running locally, the following endpoints would be accessible:

- http://localhost:8080/api/v1/telemetry/refrigeratedtruck
- http://localhost:8080/api/v1/reports/refrigeratedtruck?secondsAgo=600&temperature=45

The `/api/v1/telemetry/refrigeratedtruck` endpoint simply probes the source endpoint for the latest telemetry data and displays it in JSON form.

The `/api/v1/reports/refrigeratedtruck` will return a list of records whose
- timestamps were recorded after `secondsAgo` seconds prior to the current time (i.e. `timestamp > now - secondsAgo`) <i>and whose</i>
- temperatures match or exceed the specified value

Note that all query parameters are completely optional and will default to the values specified by the following environment variables:
- `secondsAgo` will default to `TELEMETRY_REPORTING_INTERVAL_WRITE`.
- `temperature` will default to `TELEMETRY_REPORTING_THRESHOLD_TEMPERATURE`

## Developer Notes
I decided to incorporate database support into the exercise (in addition to the file-based logging and reporting) for my own sake, since it allowed me to run sanity checks on the data I was processing.
I also felt that using Spring Batch to write the telemetry data a database and querying it for relevant records turned out to be a much more elegant solution.
The combination of Spring Batch and Spring Data made the creation of the web API extremely easy. I was able to insert records in batches into the database, and the optional query parameters also allowed for more flexible reporting (as opposed to changing environment variables or polling configuration files for changes).

I also noticed that the dummy endpoint always returned the same `id`, so I assumed that it represented the `truckID`. I accounted for that in my solution by including that additional field in my [RefrigeratedTruckTelemetry.java](https://github.com/Reclyptor/Iota/blob/master/src/main/java/com/walmart/iota/model/RefrigeratedTruckTelemetry.java) telemetry model. I also kept the original `id` field to be used as a primary key field for database operations. Thus, the `id` that comes from the dummy endpoint is mapped to the `truckID` field in the JSON payloads.