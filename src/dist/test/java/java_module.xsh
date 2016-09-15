# Demostrate use of a java module import
#
# String
import module s="java:java.lang.String"
import module java
a=s:new(Hi)
echo Value: $a
echo Length: s:length($a)



import module test="java:org.xmlsh.test.TestTypes" at ../lib/xmlsh-2.0-tests.jar

# Static method
echo test:staticAsString()


# TestTypes()
t=test:new()
echo test:getConstructor($t)


# TestTypes(String)
s=s:new(Hello)
t=test:new($s)
echo test:getConstructor($t)

# TestTypes(Object)
t=test:new($t)
echo test:getConstructor($t)

#TestTypes(Integer)
i=jnew(java.lang.Integer 10)
t=test:new({$i})
echo test:getConstructor($t)

#TestTypes(Integer,String)
i=jnew(java.lang.Integer 10)
t=test:new({$i} Hi)
echo test:getConstructor($t)


#TestTypes(Long,String)
i=jnew(java.lang.Long 10)
t=test:new({$i} HI)
echo test:getConstructor($t)

