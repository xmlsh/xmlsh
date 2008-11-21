# test of xpath

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
echo Xpath on stdin
xpath //AUTHOR < books.xml

#xpath on standard file
echo Xpath on file
xpath -i books.xml //AUTHOR

#xpath with xml expression
echo xpath on xml expression
xpath -i $D1 //bar

#xpath with substitued variables and no input
xpath -n -v '$x/foo' x <[<bar><foo>text</foo></bar>]>

#Test -q and result of xpath
xpath -n -v -q '$x/foo' x <[<bar><foo>text</foo></bar>]> && echo Success returned xpath
xpath -n -v -q '$x/spam' x <[<bar><foo>text</foo></bar>]> || echo Success empty returned xpath

# Namespace query 
# Without predeclared ns in environment
echo '<t:test xmlns:t="http://www.example.org/test">Test</t:test>' | 
xpath  -ns t=http://www.example.org/test /t:test

# Redefine the prefix in the query
echo '<t:test xmlns:t="http://www.example.org/test">Test</t:test>' | 
xpath  -ns x=http://www.example.org/test /x:test

# Query a literal document with namespace predeclared
declare namespace t=http://www.example.org/test
xquery -i <[ <t:test>Test</t:test> ]> .
echo 

xquery -i <[ document{ <t:test>Test</t:test> } ]> /t:test 
echo




