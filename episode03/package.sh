#!/bin/bash

ENV="${1:-docker}"

mvn clean package

if [ "${ENV}" = "k8s" ]; then
eval $(minikube -p minikube docker-env)
fi

docker build -t primesum-calculator -f Dockerfile ./
