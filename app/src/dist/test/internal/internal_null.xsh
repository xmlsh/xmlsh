# 
# Test internal boolean functions
. ../common

message  null() is null
xtype -s {null()}

[ {null()} ] && message true || message false 
n=null()
[ {$n} = {$n} ] && message true || message false 
