# jcat [ files ]
# concatenate json files into an array
import commands posix 

echo "["
sep=""
if [ $# -gt 0 ] ; then  
  for file ; do 
    echo $sep
    cat $file 
    sep=","
  done
else
  cat 
fi

echo "]"    
   