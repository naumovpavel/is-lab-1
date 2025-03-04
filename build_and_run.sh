#!/bin/bash

service docker start

./gradlew build war

docker build .

docker build --tag=is-lab1 .

docker run --rm --net=host is-lab1:latest
