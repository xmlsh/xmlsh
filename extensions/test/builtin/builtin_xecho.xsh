# Tests of builtin xecho 
# xecho differs from echo in that it outputs XML expressions
#
# echo a blank line
xecho 
# echo a string
xecho "hi there"
# echo an XML expression 
xecho <[<foo>bar</foo>]>
# echo an xml sequence seperated by newlines (differe then echo)
xecho <[1,"hi",<foo/>]>
# echo into a sequence
xecho <[ <foo/>,1,"bar" ]> >{_seq}
xecho <[ 3,"bletch" ]> >>{_seq}
# should be 5
echo There are <[ count($_seq) ]> elements in the sequence
echo -n No Newlines:
xecho -n <[ 'in this text' ]>
echo :Between the colons









