## Dev

### Setting up couchdb

If you have not already setup CouchDB from the previous episode, do this:
```sh
./setup-couchdb.sh
```

### Creating secrets for AWS in K8s

```sh
kubectl create secret generic sop-aws --from-literal=AWS_SECRET_ACCESS_KEY=SOMETHING/SECRET --from-literal=AWS_ACCESS_KEY_ID=SOMETHING
```
### Package the code

```sh
./package.sh
```