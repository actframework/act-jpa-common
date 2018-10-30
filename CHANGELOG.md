# act-jpa-common CHANGE LOG

1.5.2
* update to act-1.8.8
* Make injected `EntityManager` be independent of act JPA context #35
* Make JPADao be stateless
* Apply new scope for implicit transaction #28
* it shall use non-readOnly entity manager for POST/PUT/DELETE requests #27
* `JPAContext.initForJob()` issue when invoking a `Job` within an `ActionContext` #26
* `JPAQuery` - `offset` and `limit` is not effective #24
* `JPADao.first()` triggered issue when more than one records filtered #23
* `IllegalStateException` when calling `JPADao.save(entity)` method #22
* Extends Timestamp auditing to support user auditing #21
* JPADao's deleteById throw NullPointerException #15
* JPAQuery save() can not execute SQL for the first time after act startup #14

1.5.1 - 19/Jun/2018
* update act to 1.8.8-RC10
* update sql-common to 1.4.2
* Setup and Teardown Tx scope on JobContext initialized and destroyed event #11

1.5.0
* `JPAQuery.as(Type)` failed to copy parameter settings #10
* Add `delete()` method to `JPAQuery` #9
* update to act-1.8.8-RC9
* update to act-sql-common-1.4.1

1.4.0
* update to act-1.8.8-RC8
* update to act-sql-common 1.4.0
* `JPAQuery.first()` raised `NoResultException` when no result found #6
* Add `findOne` method to `JPAQuery` #7

1.3.0 - 20/May/2018
* export entity manager in JPADao #4
* update act to 1.8.8-RC7

1.2.4 - 19/May/2018
* update act to 1.8.8-RC5
* update act-sql-common to 1.3.4

1.2.3 - 02/Apr/2018
* update act to 1.8.5
* update act-sql-common to 1.3.3

1.2.2 - 25/Mar/2018
* update act to 1.8.2
* update act-sql-common to 1.3.2

1.2.1 - 11/Mar/2018
* update to act-1.8.1
* update to act-sql-common-1.3.1

1.2.0
* timestamp audit not working with base class #3
* update to act-1.8

1.1.0
* Request to support timestamp audit #2
* data not commit to database unless `@Transactional` annotation is used #1

1.0.0
* The first release - corresponding to act-1.7.x

