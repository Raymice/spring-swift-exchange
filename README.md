

# Start running the Project

This project is a Spring Boot application generated with Spring Initializr.
To run the application, use the following command:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
``` 


## Application Build Requirements
- Java 21 or higher
- Maven 3.9.11 or higher
- GraalVM 25+ (if you plan to build native images)

## Application Required Dependencies
This project requires the following dependencies:
- ActiveMQ classic 6.1.7
- Redis 8.2.2
- Postgres 17-alpine


## Profiles
This project uses Spring profiles to manage different configurations for various environments.
The available profiles are:
- `dev`: Development environment
- `test`: Testing environment
- `prod`: Production environment

## Configuration
// TODO: Add configuration details here

## How it's working
// TODO: Add explanation of how the application works here

## Performances and Scalability
- ✅The application is designed to be stateless, allowing for easy scaling and load balancing.
- ✅Multiple instances of the application can run concurrently to handle increased load.
- ✅Usage of virtual threads (Java 21) to improve concurrency and resource utilization.
- ✅ActiveMQ handles message queuing and delivery efficiently.
- ✅Redis locked operations ensure data consistency across instances.
- ✅Multiple ActiveMQ consumers can be configured to process messages in parallel, enhancing throughput.


Usage of **spring-boot-starter-opentelemetry** to support observability for Native build
Need to update properties before usage in production (opentelemetry, security, ...)

## How to build the docker image (not native)

```bash
./mvnw spring-boot:build-image -Dmaven.test.skip=true
```

## How to deploy the dependency stack

* Use script
```bash
./scripts/deploy-dependency-stack.sh
```
* Run manual command
```bash
docker-compose docker-compose.yml up -d
```

## How to deploy the full stack (include the app)

* Use script (build of app required before)
```bash
./scripts/deploy-full-stack.sh
```

* Use script including the build of the app
```bash
./scripts/build-and-deploy-full-stack.sh
```

* Run manual command (build of app required before)
```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d
```
