# delete uri ...
import module ml=org.xmlsh.marklogic 
for uri ; do
   ml:query -q "doc(\"$uri\")"
done