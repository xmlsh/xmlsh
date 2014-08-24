jset -v int -c java.lang.Integer 12345
xtype -j -v int
echo $int 
jset -v hex -c java.lang.Integer -m toHexString $int
xtype -j -v hex
echo $hex
jset -v min -c java.lang.Integer -f MIN_VALUE
xtype -j -v min
echo $min
jset -v s -c java.lang.String "Hello World"
echo $s
xtype -j -v s
jset -v upper -o {$s} -m toUpperCase
echo $upper