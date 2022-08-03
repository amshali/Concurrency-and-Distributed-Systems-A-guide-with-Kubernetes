## Build

```sh
# Start minikube
minikube start
# Tunneling for load balancer
minikube tunnel
#-------------------------------------

./package.sh

kubectl apply -f deployment.yaml

# Wait for services to come up...
kubectl get pods,services

# Find out the address of the load balancer for service/primesum-server

export SERVER=????

# Check to see if server is up:
curl -X GET "http://${SERVER}/actuator/healthz"

# Run a load test:
artillery run -t "http://${SERVER}" load-test.yaml

```