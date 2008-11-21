# Test of core namespaces support

# declare 2 namespaces
declare namespace t1=http://www.example.org/test1
declare namespace t2=http://www.example.org/test2
declare namespace

echo <[ <t1:test><t2:test>Test</t2:test></t1:test> ]>

# undeclare t2
declare namespace t2=
declare namespace

#echo Expected Error 
#echo <[ <t1:test><t2:test>Test</t2:test></t1:test> ]>

# OK
echo <[ <t1:test><test>Test</test></t1:test> ]>
