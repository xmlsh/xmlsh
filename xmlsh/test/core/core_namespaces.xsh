# Test of core namespaces support

# declare 2 namespaces
declare namespace t1=http://www.example.org/test1
declare namespace t2=http://www.example.org/test2
declare namespace
# Turn off indentation for this test
set +indent

echo <[ <t1:test><t2:test>Test</t2:test></t1:test> ]>

# undeclare t2
declare namespace t2=
declare namespace

#echo Expected Error 
#echo <[ <t1:test><t2:test>Test</t2:test></t1:test> ]>

# OK
echo <[ <t1:test><test>Test</test></t1:test> ]>

# Unset namespaces
declare namespace t2=
declare namespace t1=

#try with inband variable assignment
# DEPRECIATED
# xmlns:t3=http://www.example.org/test3 eval 'echo <[ <t3:test><test>Test</test></t3:test> ]>'
{ 
  declare namespace t3=http://www.example.org/test3
  eval 'echo <[ <t3:test><test>Test</test></t3:test> ]>'
}
# Default namespace
declare namespace http://test.xmlsh.org/ns
echo <[ <foo/> ]>
#
declare namespace ""
echo <[ <foo/> ]>

