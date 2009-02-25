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

xed -i $F -r "John Doe" -e //AUTHOR > $TF
A=$(xquery -i $TF 'distinct-values(//AUTHOR)')
[ $A = "John Doe" ] || exit Failed replacement

rm $TF

# Replace a full element from a in-core document

X=<[
<root>
   <foo a="attr">bar</foo>
   <foo2>spam</foo2>
</root>]>

xed -i $X -r <[<bletch/>]> -e /root/foo2
 
# replace attribute
_X=$<( xed -i $X -r <[ attribute {"a"} {"attr2" } ]> -e /root/foo )
equals $_X <[document{ <root><foo a="attr2">bar</foo><foo2>spam</foo2></root>} ]> && 
echo Success replace attribute

# add element
_X=$<( xed -i $X -a <[ <child/> ]> -xpath /root )

equals $_X <[document{ <root><child/><foo a="attr">bar</foo><foo2>spam</foo2></root> } ]> && 
echo Success add element

_X=$<( xed -i $X -d -matches foo2 )
equals $_X  <[document{ <root><foo a="attr">bar</foo></root> } ]> && 
echo Success delete element

# Test xproc modes -rx 
_X=$<(xed -i $X -rx "'text'" -matches foo)
equals $_X  <[document{ <root>text<foo2>spam</foo2></root> } ]> && 
echo Success xproc replace text 

_X=$<(xed -i $X -rx "'text'" -matches "@a")
equals $_X  <[document{ <root><foo a="text">bar</foo><foo2>spam</foo2></root> } ]> && 
echo Success xproc replace attriubute 



exit 0




