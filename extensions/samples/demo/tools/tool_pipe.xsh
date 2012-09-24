# Complex pipeline

cd $XMLSH/samples/styles
xslt -f identity.xsl -i ../data/books.xml |
xcat |
xquery '<all>{for $i in //ITEM return $i}</all>'  |
xpath '//ITEM[3]/AUTHOR/string()'

