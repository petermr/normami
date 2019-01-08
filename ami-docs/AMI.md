# AMI
The name 'AMI' is becoming the standard for all operations using the AMI system ('norma') will be increasingly phased out. The impetus comes from channging to the `picocli` commandline tool whcih allows much better organisaation of commands, subcommands, parameters and options. Much of the initial help will be generated from the Picocli help tool - this is almost certain to be more up-to-date than tis document.

## philosophy
AMI consists of 10+ major commands, often run consecutively. The philosophy is driven by `make` (https://en.wikipedia.org/wiki/Make_(software) ) and `map-reduce` (https://en.wikipedia.org/wiki/MapReduce ), although not universally implemented (yet). The "data" are held on the filestore (as a supertree) and generally exposed as files. The system is therefore stateless - it should define what has happened to it and what can be done with it. 

## CProject and CTrees
The core data are collections of documents and their components (often "assets"). A `CProject` is simply a directory with well-defined (reserved) names for most parts of the system. `AMI` has many tools for traversing and transforming the `CProject`. But also common tools (Python, shell, etc.) outside the `AMI` system can modify the `CProject` without breaking it - it is the data specification that is key, not the software.

A `CProject` normally consists of a set of "child" `CTree`s - themselves directories. A command such as:

```ami-pdf -p myCProject <parameters/options>
```

is implemented as:

```
for-each ctree in myCProject {
        ami-pdf ctree <parameters/options>
   }
```

