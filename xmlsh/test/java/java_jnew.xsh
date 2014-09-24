# 
# Test of jnew 
# 
import commands java
# Integer
i=jnew(java.lang.Integer 1)
xtype -j -v i
echo $i

# Date
date=jnew(java.util.Date)
xtype -j -v date

# Do NOT expand wildcards

echo jnew( java.lang.String * )
echo jnew( java.lang.String [a-z]* )



import module s=java:java.lang.String
echo s:new( * )

# Do expand vars
a=test
echo s:new(  $a )
echo jnew( java.lang.String $a )

a=*
echo s:new( $a )
echo jnew( java.lang.String $a )




