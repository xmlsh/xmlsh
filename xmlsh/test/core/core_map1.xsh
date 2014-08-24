# Test of basic map features
a={}
xtype $a
a+={ name : value }
xtype $a
a+={ name2 : "value 2" }
echo ${a[name2]}
b={ $a , "c" : { "a1" : "value1" , "a2" : value2 } }
xtype $b
echo ${b[c]}