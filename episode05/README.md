```sh

# Package:
./package.sh

# Run server:
docker run -it -p 8080:80 primesum-calculator:latest

# Run a query:
curl -s -X POST -H "Content-Type: application/json" \
  http://localhost:8080/sumPrime --data '{"a": 1, "b": 20000000}'

```