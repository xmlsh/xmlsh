# Test of mkpipe

# Test stream pipes

xmkpipe s

{ while read a  ; do echo $a ; xtype $a ; done <(s) ; echo Done ; } &

for i in <[ 1 to 100 ]> ; do 
   echo <[ <elem>{$i}</elem> ]>
done  >(s)
xmkpipe -close s
wait
echo Text Complete


xmkpipe -xml x

{ while xread a  ; do xecho $a ; xtype $a ; done <(x) ; echo Done ;  } &

for i in <[ 1 to 100 ]> ; do 
   xecho <[ <elem>{$i}</elem> ]> 
done  >(x)
xmkpipe -close x
wait
echo XML Complete