This is an extension module to xmlsh
This version requires xmlsh version 1.0.3 or greater

To run this module you must include the supplied marklogic_ext.jar
as well as the marklogic xcc.jar in the CLASSPATH with xmlsh.

To get xcc.jar 
http://developer.marklogic.com/download/

then set the environment variable MLCONNECT to be the connection string.
Example
    MLCONNECT=xcc://user:pass@host/Database

Import the marklogic extension in xmlsh with an "import module" statement.

e.g.
   XMODPATH=/usr/local/xmlsh/ext
   import module marklogic
   
or
   import module /usr/local/xmlsh/ext/marklogic/module.xml
   


the following commands are supported
  ml:invoke
  ml:put
  ml:query
  ml:del
  ml:get
  ml:list
  

Installation and getting started instructions are available at 

http://www.xmlsh.org

The "Quick Start" has directions on how to get up and running

http://www.xmlsh.org/QuickStart

