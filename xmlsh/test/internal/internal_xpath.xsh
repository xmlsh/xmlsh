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


