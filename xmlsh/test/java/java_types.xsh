#
# Test basic types using TestTypes.class
#
import commands java 
import java ../bin/xmlsh-test.jar

function getConstructor() {
  local c ;
  jset -v c -o {$1} -m getConstructor
  return {$c}
 }

# TestTypes()
jset -v types -c org.xmlsh.test.TestTypes
echo getConstructor($types)

# TestTypes(String)
s=jnew(java.lang.String Hello)
types=jnew(org.xmlsh.test.TestTypes $s)
echo getConstructor($types)

# TestTypes(Object)
types=jnew(org.xmlsh.test.TestTypes $types)
echo getConstructor($types)

#TestTypes(Integer)
i=jnew(java.lang.Integer 10)
types=jnew(org.xmlsh.test.TestTypes $i)
echo getConstructor($types)

#TestTypes(Integer,String)
i=jnew(java.lang.Integer 10)
types=jnew(org.xmlsh.test.TestTypes $i Hi)
echo getConstructor($types)


#TestTypes(Long,String)
i=jnew(java.lang.Long 10)
types=jnew(org.xmlsh.test.TestTypes $i HI)
echo getConstructor($types)

#Null
#null=types.asNull()
#xtype {$null}

# static method
jset -v types -c org.xmlsh.test.TestTypes -m staticAsString
echo $types
xtype $types

