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

       +------------------------------------+                    +------------------------------------------------------+
       |                                    |                    |                                                      |
       |  Node table: Classes / Interfaces  |                    |  Edge table: Links between objects of type CLASS     |
       |                                    |                    |                                                      |
       +-----------------+------------------+                    +-------+-------------------------+--------------------+
                         |                                               |                         |
                         |                                               |                         |
                         v                                               v                         v
    +--------------------+-----------------------------+       +---------+---------------+      +--+----------------------+
    |                                                  |       |                         |      |                         |
    |  Class                                           |       | Impl_inf                |      | Extends                 |
    |                                                  |       |                         |      |                         |
    +----+------+---------+-----+-------------+--------+       +----+-------+------------+      +----+-------+------------+
    |    |      |         |     |             |        |       |    |       |            |      |    |       |            |
    | ID | Name | Package | jar | fullyloaded | is_inf |       | ID | Clazz | impl_inf   |      | ID | Clazz | Super      |
    |    |      |         |     |             |        |       |    |       |            |      |    |       |            |
    +-+--+------+---------+-----+-------------+--------+       +----+---+---+------+-----+      +----+---+---+------+-----+
      |                                                                 ^          ^                     ^          ^
      |                                                                 |          |                     |          |
      |                                                                 |          |                     |          |
      |                                                                 |          |                     |          |
      |                                                                 |          |                     |          |
      |       foreign key relationship                                  |          |                     |          |
      +-----------------------------------------------------------------+----------+---------------------+----------+


        +----------------------------------------------------------------------+
        |                                                                      |
        |  Hybrid table: Stores nodes (method object) and edges (connection    |
        |  to class and declaring class)                                       |
        |                                                                      |
        +-------------------------------+--------------------------------------+
                                        |
                                        |
                                        v
          +-----------------------------+--------------------------------------------+
          |                                                                          |
          |  Methods                                                                 |
          |                                                                          |
          +----+------------+-------+--------+----------+-----------+-------+--------+
          |    |            |       |        |          |           |       |        |
          | ID | decl_class | class |  Name  | Signatur | decorator | final | static |
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
| is_inf| boolean | true if the class is an interface |

### Table Implements
| Column name | Data Type | Remarks |
|:-----------|:---------|:-------|
| ID | int | primary key, non null |
| Clazz | int | reference to a clazz, foreign key to Classes::ID |
| impl_inf | int | reference to a clazz, foreign key to Classes::ID, this reference must have Classes::is_inf=true  |

### Table Extends
| Column name | Data Type | Remarks |
|:-----------|:---------|:-------|
| ID | int | primary key, non null |
| Clazz | int | reference to a clazz, foreign key to Classes::ID |
| Super | int | reference to a clazz, foreign key to Classes::ID, this reference is superclass of Clazz  |

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
* Program should have a DB abstraction layer so that underlying DB can be replaced

## Other ideas

### Encapsulation with own classloader
Die Hauptklasse wird mit eigenem ClassLoader instantiiert so dass die jar-files nur in einem Verzeichnis sein und nicht händisch im CLASSPATH mitgegeben werden müssen:

    public void main (String args []) throws Exception
    {
        String inputfilename = args [0];
        File   inputfile = new File (args [0]);

        if (!inputfile.exists ()) {
            // error handling
            System.exit (1);
        }

        ClassProcessor myProcessor = null;
        Class<ClassProcessor> clazzProcessor = null;

        if (inputfile.isDirectory ()) {
            File [] jarfiles = inputfile.findFiles (String s -> return s.endsWith (".jar"));
            Vector<URL> v = new Vector<URL> ();
            for (String jarfile : jarfiles) {
                v.add (jarfile.toURI().toURL ());
            }
            clazzProcessor = Class.forName ("de.cw.deseregistry.ClassProcessor", true, 
                new URLClassLoader (v.toArray (), Main.class.getClassLoader ()));
        }
        else {
            clazzProcessor = Class.forName ("de.cw.deseregistry.ClassProcessor", true, 
                new URLClassLoader (new URL [] { inputfile.toURI().toURL (), Main.class.getClassLoader ()));
        }

        myProcessor = (ClassProcessor) clazzProcessor.newInstance ();
    }
        
### Recursive analysis of classes
Man muss letztlich, bei Klassen die eine Elterklasse haben, rekursiv analysieren. Welche Methoden braucht man dafür?

* getSuperClass
* getInterfaces
* getMethods
* getDeclaredMethods

Code:

    private void recursiveVisit (Class clazz) throws Exception
    {
        
        if (dblayer.exists (clazz)) {
            return;
        }
        
        recursiveVisit (clazz.getSuperClass ());
        
        Class [] ifaces = clazz.getInterfaces ();
        for (Class iface : ifaces) {
             recursiveVisit (iface);
        }
        
        int pkey = dblayer.add (clazz); // adds: package, name, isiface, superclass, ...
        
        Method [] methods = clazz.getDeclaredMethods ();
        
        for (Method m : methods) {
            dblayer.add (pkey, m); // adds Method Info
        }
    }

### Helper methods

* `getSignatur (Method)`: gibt mir die Signatur als String ==> vielleicht m.toString ()?
* `dblayer.exists (Class clazz)` prüft auf name und package
    
    
