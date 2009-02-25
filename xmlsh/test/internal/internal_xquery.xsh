# test of xquery

D1=<[
document {
<root a1="value1">
  <foo>
    <bar a2="value2"/>
    text
   </foo>
</root>
}
]>

cd ../../samples/data

#xpath on stdin
echo Xquery on stdin
xquery //AUTHOR < books.xml

#xpath on standard file
echo Xquery on file
xquery -i books.xml //AUTHOR

#xpath with xml expression
echo Xquery on xml expression
xquery -i $D1 //bar

#xpath with substitued variables and no input
xquery -n -v 'declare variable $x external ; $x/foo' x <[<bar><foo>text</foo></bar>]>

# more complicated xquery

xquery -f ../query/books.xquery -i books.xml

# Namespace query 
# Without predeclared ns in environment
echo '<t:test xmlns:t="http://www.example.org/test">Test</t:test>' | 
xquery -ns t=http://www.example.org/test /t:test

# Redefine the prefix in the query
echo '<t:test xmlns:t="http://www.example.org/test">Test</t:test>' | 
xquery   -ns x=http://www.example.org/test /x:test

# Query a literal document with namespace predeclared
declare namespace t=http://www.example.org/test
xquery  -i <[ <t:test>Test</t:test> ]> .

xquery -i <[ document{ <t:test>Test</t:test> } ]> /t:test 



