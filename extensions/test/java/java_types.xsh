#
# Test basic types using TestTypes.class
#

import java ../bin/xmlsh-test.jar

# TestTypes()
jset -v types -c org.xmlsh.test.TestTypes
echo types.getConstructor()  

# TestTypes(String)
s=jnew(java.lang.String Hello)
types=jnew(org.xmlsh.test.TestTypes $s)
echo types.getConstructor()

# TestTypes(Object)
types=jnew(org.xmlsh.test.TestTypes $types)
echo types.getConstructor()

#TestTypes(Integer)
i=jnew(java.lang.Integer 10)
types=jnew(org.xmlsh.test.TestTypes $i)
echo types.getConstructor()

#TestTypes(Integer,String)
i=jnew(java.lang.Integer 10)
types=jnew(org.xmlsh.test.TestTypes $i Hi)
echo types.getConstructor()


#TestTypes(Long,String)
i=jnew(java.lang.Long 10)
types=jnew(org.xmlsh.test.TestTypes $i HI)
echo types.getConstructor()

#Null
#null=types.asNull()
#xtype {$null}

# static method
jset -v types -c org.xmlsh.test.TestTypes -m staticAsString
echo $types
xtype $types

