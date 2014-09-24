# Tests of builtin echo 
#
# echo a blank line
echo 
# echo a string
echo "hi there"
# echo an XML expression 
echo <[<foo>bar</foo>]>
# echo an xml sequence seperated by space
echo <[1,"hi",<foo/>]>
# echo with variable expansion
A=var1
B=var2
unset C # just in case 

# echo with null middle variable
echo $A$C$B
# echo quoted
echo "$A$C$B"
#
# echo single quoted
echo '$A$C$B'

# Echo -n
echo -n foo ; echo bar

# Echo to a named port
echo -p output to output
echo -p xxx to xxx (xxx)>&(output)






