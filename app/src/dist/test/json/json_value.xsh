# test json:boolean
import module json=json

echo json:value( 1 )
echo json:value( <[ 1 ]> )
xtype json:value( <[ 1 ]> )
xtype json:value( <[ 1.5 ]> )
xtype json:value( text )
a=json:value()
xtype -v a

