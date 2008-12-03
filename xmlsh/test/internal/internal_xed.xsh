# test for xed command
# NOTE: xed is in its infancy

equals() 
{
    A=$1
    B=$2
 	if [ <[ fn:deep-equal( $A , $B ) ]> ] ; then
 	   return 0 
 	else
 	   return 1     	
 	fi
}



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
   <foo a="attr">bar</foo>
   <foo2>spam</foo2>
</root>]>

xed -i $X -r <[<bletch/>]> /root/foo2
echo 
# replace attribute
_X=$<( xed -i $X -r <[ attribute {"a"} {"attr2" } ]> /root/foo )
equals $_X <[document{ <root><foo a="attr2">bar</foo><foo2>spam</foo2></root>} ]> && 
echo Success replace attribute

# add element
_X=$<( xed -i $X -a <[ <child/> ]> /root )

equals $_X <[document{ <root><child/><foo a="attr">bar</foo><foo2>spam</foo2></root> } ]> && 
echo Success add element

_X=$<( xed -i $X -d /root/foo2 )
equals $_X  <[document{ <root><foo a="attr">bar</foo></root> } ]> && 
echo Success delete element

exit 0




