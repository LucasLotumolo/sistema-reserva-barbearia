## Comando para executar todas as suites

```bash
./mvnw test -Dtest="*Suite"
```

## Comando para somente uma suite

```bash
./mvnw test -Dtest=TddSuite
./mvnw test -Dtest=FunctionalSuite
./mvnw test -Dtest=UnitTestSuite
```