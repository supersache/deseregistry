# Requirements

## Registry Contents

*deseregistry* stores information about classes and relates the information about classes. About each loaded class the following information is available:

* Name of the class
* Package of the class
* which jar file has packaged the class
* could the class be loaded by `Class.forName`?
* Superclass (if any)
* Which interfaces are implemented
* Which methods are implemented 

## Table design

    +----------------------------------------------------------+              +-------------------------+
    |                                                          |              |                         |
    |  Classes                                                 |              | Impl_inf                |
    |                                                          |              |                         |
    +----+------+---------+-----+-------------+-------+--------+              +----+-------+------------+
    |    |      |         |     |             |       |        |              |    |       |            |
    | ID | Name | Package | jar | fullyloaded | super | is_inf |              | ID | Clazz | impl_inf   |
    |    |      |         |     |             |       |        |              |    |       |            |
    +-+--+------+---------+-----+-------------+-------+--------+              +----+-------+------------+
      |                                           ^                                    ^          ^
      |                                           |                                    |          |
      |                                           |                                    |          |
      |                                           |                                    |          |
      |                                           |                                    |          |
      |       foreign key relationship            |                                    |          |
      +-------------------------------------------+------------------------------------+----------+

### Table classes


| Column name | Data Type | Remarks |
|:-----------|:---------|:-------|
| ID | int | primary key, non null |
| Name | varchar | Name of the class, i.e. String |
| Package | varchar | Package of the class, i.e. java.util |
| jar | varchar | Name of the jar-file the class was loaded from, i.e. commons-collections-1.3.1.jar |
| fullyloaded | boolean | false if Class.forName throws an exception, true otherwise |
| super | int | ID of the superclass or NULL |
| is_inf| boolean | true if the class is an interface |

### Table Impl_inf
| Column name | Data Type | Remarks |
|:-----------|:---------|:-------|
| ID | int | primary key, non null |
| Clazz | int | reference to a clazz, foreign key to Classes::ID |
| impl_inf | int | reference to a clazz, foreign key to Classes::ID, this reference must have Classes::is_inf=true  |
