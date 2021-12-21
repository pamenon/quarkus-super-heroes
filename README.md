# Quarkus Superheroes Sample

## Table of Contents
- [Introduction](#introduction)
- [Running Locally via Docker Compose](#running-locally-via-docker-compose)
- [Deploying to Kubernetes](#deploying-to-kubernetes)

## Introduction

This is a sample application demonstrating Quarkus features and best practices. The application allows superheroes to fight against supervillains. The application consists of several microservices, communicating either synchronously via REST or asynchronously using Kafka:
- [Super Hero Battle UI](ui-super-heroes)
    - An Angular application to pick up a random superhero, a random supervillain, and makes them fight.
    - The Super Hero UI is exposed via Quarkus and invokes the Fight REST API.
- [Villain REST API](rest-villains)
    - A classical HTTP microservice exposing CRUD operations on Villains, stored in a PostgreSQL database.
    - Implemented with blocking endpoints using [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive) and [Quarkus Hibernate ORM with Panache's active record pattern](https://quarkus.io/guides/hibernate-orm-panache).
- [Hero REST API](rest-heroes)
    - A reactive HTTP microservice exposing CRUD operations on Heroes, stored in a PostgreSQL database.
    - Implemented with reactive endpoints using [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive) and [Quarkus Hibernate Reactive with Panache's repository pattern](http://quarkus.io/guides/hibernate-reactive-panache).
- [Fight REST API](rest-fights)
    - A REST API invoking the Hero and Villain APIs to get a random superhero and supervillain. Each fight is then stored in a PostgreSQL database.
    - Implemented with reactive endpoints using [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive) and [Quarkus Hibernate Reactive with Panache's active record pattern](http://quarkus.io/guides/hibernate-reactive-panache).
    - Invocations to the Hero and Villain APIs are done using the [reactive rest client](https://quarkus.io/guides/rest-client-reactive) and are protected using [resilience patterns](https://quarkus.io/guides/smallrye-fault-tolerance), such as retry, timeout, and circuit breaking.
    - Each fight is asynchronously sent, via Kafka, to the Statistics microservice
- [Statistics](event-statistics)
    - Stores statistics about each fight and serves them to an HTML + JQuery UI using [WebSockets](https://quarkus.io/guides/websockets).
- Prometheus
    - Polls metrics from the Fight, Hero, and Villain microservices.

Here is an architecture diagram of the application:
![Superheroes architecture diagram](images/application-architecture.png)

The main UI allows you to pick one random Hero and Villain by clicking on _New Fighters_. Then, click _Fight!_ to start the battle. The table at the bottom shows the list of previous fights.
![Fight screen](images/fight-screen.png)

## Running Locally via Docker Compose
Pre-built images for all of the applications in the system can be found at [`quay.io/quarkus-super-heroes`](http://quay.io/quarkus-super-heroes).

Pick one of the 4 versions of the application from the table below and execute the appropriate docker compose command from the `quarkus-super-heroes` directory.

   > **NOTE**: You may see errors as the applications start up. This may happen if an application completes startup before one if its required services (i.e. database, kafka, etc). This is fine. Once everything completes startup things will work fine.
   >
   > There is a [`watch-services.sh`](scripts/watch-services.sh) script that can be run in a separate terminal that will watch the startup of all the services and report when they are all up. 

| Description                  | Image Tag              | Docker Compose Run Command                                                      |
|------------------------------|------------------------|---------------------------------------------------------------------------------|
| JVM Java 11                  | `java11-latest`        | `docker-compose -f deploy/docker-compose/java11.yml up --remove-orphans`        |
| JVM Java 17                  | `java17-latest`        | `docker-compose -f deploy/docker-compose/java17 up --remove-orphans`            |
| Native compiled with Java 11 | `native-java11-latest` | `docker-compose -f deploy/docker-compose/native-java11.yml up --remove-orphans` |
| Native compiled with Java 17 | `native-java17-latest` | `docker-compose -f deploy/docker-compose/native-java17.yml up --remove-orphans` |

Once started the main application will be exposed at `http://localhost:8080`. If you want to watch the [Event Statistics UI](event-statistics), that will be available at `http://localhost:8085`.

## Deploying to Kubernetes
Pre-built images for all of the applications in the system can be found at [`quay.io/quarkus-super-heroes`](http://quay.io/quarkus-super-heroes).

Deployment descriptors for these images are provided in the [`deploy/k8s`](deploy/k8s) directory. There are versions for [OpenShift](https://www.openshift.com), [Minikube](https://quarkus.io/guides/deploying-to-kubernetes#deploying-to-minikube), and [Kubernetes](https://www.kubernetes.io).

The only real difference between the Minikube and Kubernetes descriptors is that all the application `Service`s in the Minikube descriptors use `NodePort` type so that a list of all the applications can be obtained simply by running `minikube service list`.

### Routing
Both the Minikube and Kubernetes descriptors also assume there is an [Ingress Controller](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/) installed and configured. There is a single `Ingress` in the Minikube and Kubernetes descriptors denoting `/` and `/api/fights` paths. You may need to add/update the `host` field in the `Ingress` as well in order for things to work.

Both the [`ui-super-heroes`](ui-super-heroes) and the [`rest-fights`](rest-fights) applications need to be exposed from outside the cluster. On Minikube and Kubernetes, the [`ui-super-heroes`](ui-super-heroes) Angular application communicates back to the same host and port as where it was launched from under the `/api/fights` path. 

On OpenShift, the URL containing the `ui-super-heroes` host name is replaced with `rest-fights`. This is because the OpenShift descriptors use `Route` objects for gaining external access to the application. In most cases, no manual updating of the OpenShift descriptors is needed before deploying the system. Everything should work as-is.

Additionally, there is also a `Route` for the [`event-statistics`](event-statistics) application. On Minikube or Kubernetes, you will need to expose the [`event-statistics`](event-statistics) application, either by using an `Ingress` or doing a `kubectl port-forward`. The [`event-statistics`](event-statistics) application runs on port `8085`.

### Versions
Pick one of the 4 versions of the system from the table below and deploy the appropriate descriptor from the [`deploy/k8s` directory](deploy/k8s). Each descriptor contains all of the resources needed to deploy a particular version of the entire system.

   > **NOTE:*** These descriptors are **NOT** considered to be production-ready. They are basic enough to deploy and run the system with as little configuration as possible. The databases and Kafka broker deployed are not highly-available and do not use any Kubernetes operators for management/monitoring. They also only use ephemeral storage.
   >
   > For production-ready Kafka brokers, please see the [Strimzi documentation](https://strimzi.io/) for how to properly deploy and configure production-ready Kafka brokers on Kubernetes.

| Description                  | Image Tag              | OpenShift Descriptor                                                    | Minikube Descriptor                                                   | Kubernetes Descriptor                                                     |
|------------------------------|------------------------|-------------------------------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------------------------------|
| JVM Java 11                  | `java11-latest`        | [`java11-openshift.yml`](deploy/k8s/java11-openshift.yml)               | [`java11-minikube.yml`](deploy/k8s/java11-minikube.yml)               | [`java11-kubernetes.yml`](deploy/k8s/java11-kubernetes.yml)               |
| JVM Java 17                  | `java17-latest`        | [`java17-openshift.yml`](deploy/k8s/java17-openshift.yml)               | [`java17-minikube.yml`](deploy/k8s/java17-minikube.yml)               | [`java17-kubernetes.yml`](deploy/k8s/java17-kubernetes.yml)               |
| Native compiled with Java 11 | `native-java11-latest` | [`native-java11-openshift.yml`](deploy/k8s/native-java11-openshift.yml) | [`native-java11-minikube.yml`](deploy/k8s/native-java11-minikube.yml) | [`native-java11-kubernetes.yml`](deploy/k8s/native-java11-kubernetes.yml) |
| Native compiled with Java 17 | `native-java17-latest` | [`native-java17-openshift.yml`](deploy/k8s/native-java17-openshift.yml) | [`native-java17-minikube.yml`](deploy/k8s/native-java17-minikube.yml) | [`native-java17-kubernetes.yml`](deploy/k8s/native-java17-kubernetes.yml) |

Each application is exposed outside of the cluster on port `80`.
