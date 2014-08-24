function restart ()
{
    echo list
    for group ; do 
         for x in a b c  ; do
           echo $group $x  
       done
    done 
}

restart 1 2 3

# Test variant with NL after ;

set a b c
for x ; 
do 
echo $x
done

for x 
do 
echo $x
done

for x ; do 
echo $x
done
set a1 a2 a3 
for x ; do echo $x ; done

for x
do echo $x ; done
