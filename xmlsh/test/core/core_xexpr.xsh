# Test XML Expressions

A=value
B=attribute
# turn off indentation for this test
set +indent

# empty sequence
echo empty sequence <[()]>

# simple mixed sequence
echo <[1,2,"string",<foo>bar</foo>]>

# constructed element 
#
echo the following crashes saxon sometimes
X=<[<foo a1='{$B}'>{$A}</foo>]>
#X=<[<foo>{$A}</foo>]>
echo <[<bar>{$X}</bar>]>

# for loop with sequences
# each xexpr is treated as 1 expression
for e in <[1,"string",<simple/>,$X]> <[2,3,4]>; do
   echo for: $e
done

# Positional paramters as expressions
set $X <["second","arg"]>

echo Number of params: $#
if [ $# -ne 2 ] ; then 
   echo Wrong number of positional parameters ;
   exit 1
fi

echo $1
echo $2

exit 0



