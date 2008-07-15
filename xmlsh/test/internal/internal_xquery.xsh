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




