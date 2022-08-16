#!/bin/bash

eval $(minikube -p minikube docker-env)

cd cache || exit

mvn clean package

cd ..

cd server || exit

mvn clean package
docker build -t cache-server -f Dockerfile ./

cd ..

cd worker || exit

mvn clean package
docker build -t cache-worker -f Dockerfile ./

cd ..
