package com.github.amshali.asynchronous.sumofprime;

import org.ektorp.CouchDbConnector;
import org.ektorp.Options;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import java.net.MalformedURLException;

public class CouchDbResponseRepository implements ResponseStore {
  private final CouchDbConnector db;

  public CouchDbResponseRepository() throws MalformedURLException {
    var envVars = System.getenv();
    var username = envVars.get("COUCH_DB_USERNAME");
    var password = envVars.get("COUCH_DB_PASSWORD");
    var couchDbAddress = envVars.get("COUCH_DB_ADDRESS");
    var couchDbPort = envVars.getOrDefault("COUCH_DB_PORT", "5984");
    var responseDatabase = envVars.getOrDefault("COUCH_DB_RESPONSE_DB", "response");

    var httpClient = new StdHttpClient.Builder()
        .url("http://%s:%s@%s:%s".formatted(username, password, couchDbAddress, couchDbPort))
        .build();
    var dbInstance = new StdCouchDbInstance(httpClient);
    db = new StdCouchDbConnector(responseDatabase, dbInstance);
    db.createDatabaseIfNotExists();
  }

  public static void main(String[] args) throws MalformedURLException {
    var db = new CouchDbResponseRepository();
    var a = db.getResponse("idaaa6");
    System.out.println(a);
    var b = db.getResponse("id5");
    b.setStatus(SumPrimeResponse.Status.IN_PROGRESS);
    db.updateResponse(b);
    System.out.println(db.getResponse("id5"));
  }

  @Override
  public void updateResponse(SumPrimeResponse response) {
    db.update(response);
  }

  @Override
  public SumPrimeResponse getResponse(String requestId) {
    return db.get(SumPrimeResponse.class, requestId);
  }
}
