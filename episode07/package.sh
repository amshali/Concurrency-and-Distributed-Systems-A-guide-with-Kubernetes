#!/bin/bash

eval $(minikube -p minikube docker-env)

cd cache

mvn clean package

cd ..

cd server

mvn clean package
docker build -t cache-server -f Dockerfile ./

cd ..

cd worker

mvn clean package
docker build -t cache-worker -f Dockerfile ./

cd ..
