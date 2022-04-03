# MTE
## _Monitor and Track Everything_

Performance Buddy is a one-stop destination for all KPI and performance tracking. Instead of having to fiddle around multiple applications, the user just has to open a single dashboard to view all required KPIs.
MTE-api is the backend solution for performance buddy. MTE-api, [MTE-ui](https://github.com/Ashwin-Raj18/MTE-ui), [RedisJSON](https://github.com/RedisJSON/RedisJSON) are the parts of performance buddy's microservices.

MTE-api performs two functionalities.
1. Runs scheduled cron job to fetch the data from different KPI sources like SonarQube, BlackDuck, Jira and feeds to Redis data source.
2. Exposes REST endpoint to fetch the processed data from data source.

## Architecture


![mte-arch drawio](https://user-images.githubusercontent.com/63547678/161439345-18892c25-59b5-4145-b7b1-824057037765.png)


## Installation and configuration

Performace buddy can be installed in all the platforms using Docker.
We provide docker images for MTE-api and MTE-ui.

###Create a docker network
```sh
docker network create mte-network
```

###Deploy RedisJSON docker container

```sh
#create volume for redisjson
docker volume create redis-mte-vol
#run redisjson container
docker run -d -p 6379:6379 --name redisjson-mte --network mte-network -v redis-mte-vol:/data redislabs/rejson:latest
```

###Deploy MTE-api docker container

_MTE-api needs environment varibles_

| Environmental Varbles            | description                |
|----------------------------------|----------------------------|
| SQ_URL                           | SONARQUBE URL              |
|SQ_TOKEN                          |SONARQUBE_TOKEN             |
|JIRA_URL                          |JIRA URL                    |
|JIRA_USERNAME                     |JIRA USERNAME               |
|JIRA_TOKEN                        |JIRA TOKEN                  |
|BD_URL                            |BLACKDUCK URL               |
|BD_TOKEN                          |BLACKDUCK TOKEN             |

```sh
#run mte-api container with necessary env variables
sudo docker run -d -p 8095:8080 --name mte-app --network mte-network -e REDIS_URL='redisjson-mte:6379' -e SQ_URL={YOUR_SONARQUBE URL} -e SQ_TOKEN={YOUR_SONARQUBE_TOKEN} -e JIRA_URL={YOUR_JIRA_URL} -e JIRA_USERNAME={YOUR_JIRA_USERNAME} -e JIRA_TOKEN={YOUR_JIRA_TOKEN} -e BD_URL={YOUR_BLACKDUCK_URL} -e BD_TOKEN={YOUR_BLACKDUCK_TOKEN} ashwinraj18/mte-api:1.0
```

