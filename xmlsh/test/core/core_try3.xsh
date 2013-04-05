# try/catch/finally test 2
# Test try in a while

while true ; do
    echo in while 
    try { 
       throw "Thrown" 
    } 
    catch E       {  # Test { on same line of catch as 2nd statement in a compound group
       echo Caught $E
    }
    break ;
done



