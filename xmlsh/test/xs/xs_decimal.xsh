#
# Test of xs:integer
# Test of xs:integer

import commands xs=xs

d=xs:decimal( 25.2 )
xtype $d
echo $d

d=xs:decimal( <[ xs:decimal( 2.54 ) ]> )
xtype $d
echo $d

d=xs:decimal( xs:string(.001) )
xtype $d
echo $d


d=xs:decimal( xs:integer(1) )
xtype $d
echo $d




