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
mvn package exec:exec
```

 ![#f03c15](https://placehold.it/15/f03c15/000000?text=+) If this is the first time, please run without tests (tests depend on the initialized database)

```
mvn package exec:exec -DskipTests
```

Alternatively, run with the pre-compiled jar (Compiled by Java 8)
```
java -jar moneytransfer-0.0.1-alpha.jar true
```


## Run Testcase:
Run
```
mvn test
```


## Test Procedure:

When starting up server, it initializes database with default 2 users(id: 1,2) and 2 accounts(id: 1,2).

The init accounts don't have balance.

Suggested testing tool: Postman or cUrl

Suggested steps for testing:

base url : http://localhost:8080/v1

1) call [POST] /deposit, to deposit some money to both accounts

2) (OPTIONAL) call [POST] /withdraw, withdraw money

3) call [POST] /transfer,transfer money between the accounts

4) call [GET] /transaction/{accountId} to review recent transactions.
