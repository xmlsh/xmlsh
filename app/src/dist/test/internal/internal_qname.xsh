# 
# Tests of QName function
set +indent

echo QName(http://foo.bar spam)
echo QName(prefix http://foo.bar spam)
echo QName(foo "" spam)



a=QName(http://foo.bar bar)
xtype $a
echo <[ element {$a}{"hi"} ]>
a=QName(prefix1 http://foo.bar1 bar)
b=QName(prefix2 http://foo.bar2 spam)
echo <[ element {$b} { attribute {$a} { "value" , "hi" } } ]>

