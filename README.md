# EALoader
Command application that allows loading information from PROVYS metadata catalogue to Enterprise Architect repository.

### Prerequisites

* JDK 11 & Maven 3 installed
* [Sparx Enterprise Architect](http://www.sparxsystems.com/products/ea/index.html) installed

### Build

Build requires JDK 11 & Maven 3 installed on Widnows box

[Sparx Enterprise Architect](http://www.sparxsystems.com/products/ea/index.html) must be installed

Install the Enterprise Architect API jar file, which can be found in the `Java API` directory in the EA installation to local maven repository. The standard location for this is: `C:\Program Files\Sparx Systems\EA\Java API`. Do the following:

        $ cd <the directory where the **eaapi.jar** is located>
        $ mvn install:install-file -DgroupId=org.sparx -DartifactId=eaapi -Dversion=1.0.0 -Dpackaging=jar -Dfile=eaapi.jar

### Deployment

Copy **SSJavaCOM.dll** library from `C:\Program Files (x86)\Sparx Systems\EA\Java API` directory to `C:\Windows\system32`

Application has to be run on Windows x86 (32-bit) JRE.
