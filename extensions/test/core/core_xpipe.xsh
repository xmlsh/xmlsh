# Test of xpipes 

# first test string pipes

xecho <[ 1,"hello",<elem/>,document { <doc/> } , attribute attr {"b"} ]> | 
while read a ; do xtype $a ; done 

# Now with xpipes 
set -xpipe
xecho  <[ 1,"hello",<elem/>,document { <doc/> } , attribute attr {"b"} ]> | 
while xread a ; do xtype $a ; done 
