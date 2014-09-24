import commands posix
import module j=json

OUT=$(mktemp -d)

cd ../../samples


j:jxon -o $OUT -v -xsd books2.xsd > $OUT/all.xml 
xslt -f $OUT/tojson.xsl < books.xml > $OUT/books.jxml
xsdvalidate "http://www.xmlsh.org/jxml ../schemas/jxml.xsd" $OUT/books.jxml && xml2json -p < $OUT/books.jxml > $OUT/books.json

json2xml < $OUT/books.json | xtee $OUT/temp.jxon | xslt -f $OUT/toxml.xsl > $OUT/temp.xml

# cat $OUT/temp.xml

if xcmp -x -b books.xml $OUT/temp.xml ; then
        echo Round trip succeeded
else
        echo Round trip not valid
fi

# echo $OUT
rm -r -f $OUT