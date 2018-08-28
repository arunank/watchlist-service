# WatchList service API

Tech stack used
    - Scala
    - Akka
    - Actor system
    - Spray JSON
    - ScalaTest

## Instruction to run this api server

Prerequisites
    - SBT
    - Scala

Unzip "watchlist-service"

From root project folder 'watchlist-service'
On commond prompt ->
$sbt run

WatchListServer is main class

Assumptions:
User 5 digit is assumed

Access URL:

GET - On any browser
http://localhost:8080/watchlist/123

POST
- please refer test case WatchListServiceTest

DELETE
- please refer test case WatchListServiceTest

