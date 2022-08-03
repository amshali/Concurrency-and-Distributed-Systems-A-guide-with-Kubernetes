#!/bin/bash

eval $(minikube -p minikube docker-env)

cd common

mvn clean package

cd ..

cd server

mvn clean package
docker build -t primesum-server -f Dockerfile ./

cd ..

cd worker

mvn clean package
docker build -t primesum-worker -f Dockerfile ./

cd ..
