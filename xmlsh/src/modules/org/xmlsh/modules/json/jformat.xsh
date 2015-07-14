##
## jformat [ file ]
set -indent
if [ $# = 0 ] ; then 
  :jsonread J 
  printvar J
else
  do :jsonread J  < "$1"
    printvar J
  while shift 
fi
  