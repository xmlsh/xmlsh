# XPath demo
BOOK=$XMLSH/samples/data/books.xml 
echo The author of The Big Over Easy is
xpath -i  $BOOK "//ITEM[TITLE='The Big Over Easy']/AUTHOR/string()"

