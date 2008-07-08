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
echo Xpath on xml expression
xpath -i $D1 //bar

