##
## jformat [ file ]
set -indent
if [ $# = 0 ] ; then 
  :jsonread J 
  printvar J
else
  while [ $# -gt 0 ] ; do 
    :jsonread J  < "$1" 
    printvar J
    shift 
  done
fi
  
