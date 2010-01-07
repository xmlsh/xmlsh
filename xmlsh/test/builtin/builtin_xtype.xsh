# Test of the xtype command

xtype 1 <[ 2 ]> <[ 2.5 ]> "foo" <[ <bar/> ]>
xtype <[ document{ <foo/> } ]> 
a=string
b=(a list of strings)
xtype $a $b
xtype "" <[ () ]> 

# Check that types of assigned variables keep their types
a=<[ xs:integer(1) ]>
b=$<(xquery -n -q '2.5')
xtype $a $b


