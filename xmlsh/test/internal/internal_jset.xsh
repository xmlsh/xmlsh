# Test of jset

# Simple string

# empty string
jset -v s1 -c java.lang.String
xtype $s1
echo String Value: $s1

# string with value
jset -v s1 -c java.lang.String "Hi"
echo String Value: "(init)" $s1

# Append
jset -v s1 -o $s1 -m concat " There"
echo String Value: "(concat)"  $s1

# Replace (2 args)
jset -v s2 -o $s1 -m replace "Hi" "Bye"
echo String Value: "(replace)" $s2

# Integer values
jset -v i1 -o $s1 -m length
xtype $i1
echo Length: $i1

# Import test
import java ../bin/xmlsh-test.jar

# Default constructor
jset -v t1 -c org.xmlsh.test.TestTypes
xtype $t1
jset -v m1 -o $t1 -m getConstructor

jset -v t1 -c org.xmlsh.test.TestTypes "String"
jset -v m1 -o $t1 -m getConstructor
echo $m1

jset -v t1 -c org.xmlsh.test.TestTypes $i1
jset -v m1 -o $t1 -m getConstructor
echo $m1




