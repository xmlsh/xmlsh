#
# Test of string
import commands xs=xs
s=xs:string(Hi There)
# one xs:string
xtype "$s" 
# two xs:strings
xtype $s

# looks the same
echo $s
echo "$s"
# 1 8 true
echo <[ count($s), string-length($s) , $s instance of xs:string ]>