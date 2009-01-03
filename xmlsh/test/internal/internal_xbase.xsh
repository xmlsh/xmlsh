# test of xbase
xbase < http://test.xmlsh.org/data/books.xml
X=$<(xaddbase < http://test.xmlsh.org/data/books.xml)
xbase -i <[ $X/BOOKLIST ]>