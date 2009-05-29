y This is an extension module to xmlsh
To run this module you must include the supplied calabash_ext.jar
as well as the following jar files required for calabash



calabash.jar
	Calabash runtime from http://xmlcalabash.com/download/
	
Calabash optionally uses the following additional files beyond what is included in xmlsh

commons-httpclient-3.1.jar
commons-logging-1.1.1.jar
commons-logging-api-1.1.1.jar
commons-codec-1.3.jar

See http://hc.apache.org/httpclient-3.x/dependencies.html for list of dependancies
For HTTP support include the Commons HTTP client from http://hc.apache.org/downloads.cgi
Also requires the commons logging jar from http://commons.apache.org/downloads/download_logging.cgi
Also requries common codec from http://commons.apache.org/downloads/download_codec.cgi


isorelax.jar
For relax-ng support, ISO Relax from http://sourceforge.net/projects/iso-relax/

Other jar's may be needed for optional components, see the calabash web site for details
msv.jar
tagsoup-1.2.jar

For PDF support  you may need a free or commercial license for RenderX XEP from http://services.renderx.com/Content/tools/xep.html
xep.jar 








Import the calabash extension in xmlsh with an "import module" statement
The module can be found using the XMODPATH variable or explicitly by specifying the module.xml file

e.g.
   XMODPATH=/usr/local/xmlsh/ext
   import module calabash
   
or
   import module /usr/local/xmlsh/ext/calabash/module.xml
   


the following commands are supported
  xproc
  
  

Installation and getting started instructions are available at 

http://www.xmlsh.org

The "Quick Start" has directions on how to get up and running

http://www.xmlsh.org/QuickStart

