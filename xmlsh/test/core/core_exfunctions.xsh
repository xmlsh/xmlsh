# Test of xmlsh xpath/ex functions
#
# Note this is an experimental feature
#

# NO need to predeclare for builtin 
xecho <[ xmlsh:eval("xecho $*" , ("foo" , <bar/> , 1 )  )  ]>

# Need to declare for running xquery 
declare namespace xmlsh=http://www.xmlsh.org/extfuncs

xquery -n 'xmlsh:eval("echo -n No Args")'
var=<[ xmlsh:eval("xecho <[ <foo>bar</foo> ]&gt; ") ]>
xecho $var
xread a < ../../samples/data/books.xml
xecho <[ $a//BOOKS[1]/ITEM[1]/xmlsh:eval("xcat")/ISBN ]>
xecho <[ xmlsh:eval("xcat",(),$a//BOOKS[1]/ITEM[1])/ISBN ]>
