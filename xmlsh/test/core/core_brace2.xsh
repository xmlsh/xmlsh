# core_brace2.xsh
# Testing more complicated brace syntax

# Test case - was failing on 9/17/2007]
# <version build="20090917 1037" relsase="b1_20090917"/>
for a in a b c ; do
echo In for 
{
 	echo Value is $a
}
done