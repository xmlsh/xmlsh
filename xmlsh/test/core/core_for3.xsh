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