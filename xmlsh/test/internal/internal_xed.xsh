# test for xed command
# NOTE: xed is in its infancy

F=../../samples/data/books.xml
TF=$TMPDIR/_xmlsh_temp.xml



# replace all AUTHOR's text with John Doe

xed -i $F -r "John Doe" //AUTHOR > $TF
A=$(xquery -i $TF 'distinct-values(//AUTHOR)')
[ $A = "John Doe" ] || exit Failed replacement

rm $TF

# Replace a full element from a in-core document

X=<[
<root>
   <foo>bar</foo>
   <foo2>spam</foo2>
</root>]>

xed -i $X -r <[<bletch/>]> /root/foo2

exit 0




