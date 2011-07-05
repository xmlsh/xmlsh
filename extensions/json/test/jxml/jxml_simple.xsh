import commands posix
import module j=json

OUT=$(mktemp -d)
# echo  $OUT 1>&2
cd ../../samples


j:jxon -o $OUT -v -xsd jxml_simple.xsd > $OUT/all.xml 
xslt -f $OUT/tojson.xsl < jxml.xml > $OUT/jxml.jxml
xsdvalidate "http://www.xmlsh.org/jxml ../schemas/jxml.xsd" $OUT/jxml.jxml && xml2json -p < $OUT/jxml.jxml > $OUT/jxml.json

json2xml < $OUT/jxml.json | xtee $OUT/temp.jxon | xslt -f $OUT/toxml.xsl > $OUT/temp.xml

# cat $OUT/temp.xml

if xcmp -x -b jxml.xml $OUT/temp.xml ; then
        echo Round trip succeeded
else
        echo Round trip not valid
fi

#echo $OUT
rm -r -f $OUT