# Simple while command loop


var=a
while [ $var != "aaa" ] ; do
   echo var is $var
   var=${var}a
done 
