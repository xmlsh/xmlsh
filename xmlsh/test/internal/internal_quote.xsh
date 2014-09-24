# Test of quote function

echo quote( 'hi"there' )
echo quote( <[ <foo>bar</foo> ]> )
echo quote( a b c <[ 123 ]> <[ <foo a="b"/> ]> )
echo quote( a\\b )

 