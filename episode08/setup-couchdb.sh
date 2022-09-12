# Create username and password for CouchDB:
kubectl create secret generic sop-couchdb \
  --from-literal=erlangCookie=erlang \
  --from-literal=adminUsername=foo \
  --from-literal=adminPassword=bar \
  --from-literal=cookieAuthSecret=baz

# Install CouchDB using Helm:
helm install couchdb/couchdb \
  --set createAdminSecret=false \
  --set couchdbConfig.couchdb.uuid=$(uuidgen | tr -d -)

# Run the initial setup for CouchDB:
kubectl exec --namespace default -it sop-couchdb-0 -c couchdb -- \
  curl -s http://127.0.0.1:5984/_cluster_setup \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"action": "finish_cluster"}' \
  -u foo
# You will be prompted for password after running the
# above command. Make sure to enter that(bar).