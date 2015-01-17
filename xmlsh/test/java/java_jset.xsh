# Test of jset
import module java
# Simple string
. ../common
# empty string

jset -v s1 -c java.lang.String
echo -n loc() ""
xtype {$s1}
echo loc() String Value: $s1

# string with value
jset -v s1 -c java.lang.String "Hi"
echo loc() String Value: "(init)" $s1

# Append
jset -v s1 -o {$s1} -m concat " There"
echo loc() String Value: "(concat)"  $s1

# Replace (2 args)
jset -v s2 -o {$s1} -m replace "Hi" "Bye"
echo loc() String Value: "(replace)" $s2

# Integer values
jset -v i1 -o {$s1}-m length
echo -n loc() ""
xtype {$i1}
echo loc()  Length: $i1

# Import test
import java ../bin/xmlsh-test.jar

# Default constructor
jset -v t1 -c org.xmlsh.test.TestTypes
echo -n loc() ""
xtype {$t1}
jset -v m1 -o {$t1} -m getConstructor

# String constructor
jset -v t1 -c org.xmlsh.test.TestTypes "String"
jset -v m1 -o {$t1} -m getConstructor
echo loc() $m1

# Integer constructor
jset -v t1 -c org.xmlsh.test.TestTypes $i1
jset -v m1 -o {$t1} -m getConstructor
echo loc()  $m1


# Overloaded multi arg constructors
jset -v t2 -c org.xmlsh.test.TestTypes <[ 1 ]> "String"
jset -v m2 -o {$t2} -m getConstructor
echo loc() $m2





