# Requirements & Design

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
      
      
        +--------------------------------------------------------------------------+
        |                                                                          |
        |  Methods                                                                 |
        |                                                                          |
        +----+------------+-------+--------+----------+-----------+-------+--------+
        |    |            |       |        |          |           |       |        |
        | ID | decl_class | class |   Name | Signatur | decorator | final | static |
        |    |            |       |        |          |           |       |        |
        +----+------------+-------+--------+----------+-----------+-------+--------+

      

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

### Table Methods
| Column name | Data Type | Remarkds |
| :----------|:----|:-----|
| ID | int | primary key, non null |
| decl_class | int | Class that is declaring the method, foreign key to Classes::ID |
| class | int | Class method belongs to, foreign key to Classes::ID |
| Name | varchar | Name of the method |
| Signatur | varchar | signature of the method |
| decorator | int | public, protected or private |
| final | boolean | true if final otherwise false |
| static | boolean | true if static otherwise false |


## Other requirements

* Program should be started from the command line with a single jar file or a directory which contains jar files as parameter
* While loading the classes, there should be a status line indicating the following information:
  * percentage of already loaded classes (if not possible percentage of loaded jar files)
  * number of classes with attribute `fullyloaded`
  * number of classes without `fullyloaded`
* Number of errors
* Number of warnings
