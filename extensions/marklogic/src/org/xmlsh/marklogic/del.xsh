# delete uri ...
import module ml=org.xmlsh.marklogic 
for uri ; do
   ml:query -q "xdmp:document-delete(\"$uri\")"
done