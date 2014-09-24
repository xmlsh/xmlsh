import commands posix
import module j=json

OUT=$(mktemp -d)

cd ../../samples


j:jxon -o $OUT -v -xsd jxml_none.xsd > $OUT/all.xml 
xslt -f $OUT/tojson.xsl < jxml.xml > $OUT/jxml.jxml
xsdvalidate "http://www.xmlsh.org/jxml ../schemas/jxml.xsd" $OUT/jxml.jxml && xml2json -p < $OUT/jxml.jxml 
#echo $OUT
rm -r -f $OUT