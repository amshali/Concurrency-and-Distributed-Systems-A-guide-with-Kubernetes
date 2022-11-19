#!/bin/bash

ENV="${1:-docker}"

if [ "${ENV}" = "k8s" ]; then
eval $(minikube -p minikube docker-env)
fi

cd server || exit

mvn clean package
docker build -t primesum-async-server-sqs -f Dockerfile ./

cd ..
