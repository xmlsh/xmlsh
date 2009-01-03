# test for xaddbase
X=$<(xaddbase < http://test.xmlsh.org/data/books.xml)
echo base is <[ $X/BOOKLIST/@xml:base/string() ]>
X=$<(xaddbase < http://test.xmlsh.org/data/xml-base-test.xml)
echo inner base is <[ $X/doc/div/@xml:base/string() ]>
unset X
exit 0