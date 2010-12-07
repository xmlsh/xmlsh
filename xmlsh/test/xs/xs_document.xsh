#
# Test of xs:element
import commands xs=xs

# Simple document with a root element

d=xs:document( xs:element( root ) ) 

xtype $d
xecho $d

   