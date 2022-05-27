#!/bin/bash

mvn clean package
docker build -t primesum-calculator -f Dockerfile ./
