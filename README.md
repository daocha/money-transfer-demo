# Demonostration of money transfer


## Major Libraries:

- light-rest-4j
- sqlite-jdbc

## Datastore
- SQLite

## API Documentation
https://daocha.github.io/money-transfer-demo/

## Start Server: 
Run 
```
mvn clean package exec:exec
```

## Test:
Run
```
mvn test
```


## Test Procedure:

When starting up server, it initializes database with default 2 users(id: 1,2) & 2 accounts(id: 1,2) records.

The init account doesn't have balance.

Suggested steps for testing:

1) deposit some money to both accounts

2) [optional] withdraw money

3) transfer money between the accounts
