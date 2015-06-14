# core_vars3.xsh
# Test multi var prefixes

a=b eval 'echo a=$a'
b= xtype -v b
a=b c=d e= eval 'echo a:$a c:$c e:$e'

