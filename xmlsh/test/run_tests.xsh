# Runs all tests 
# checking for required externals

# Use internal posix module
import package org.xmlsh.commands.posix 

EXTERNS=<["ls","touch","rm","cat","sleep","chmod"]>
echo Some of these tests require access to the internet
echo only files from http://test.xmlsh.org are accessed
echo Checking for required external tools $EXTERNS
# echo in path $PATH
if ! xwhich -n $EXTERNS ; then
	echo Required external programs not found
	exit 1
fi


for d in core builtin internal; do
   echo "running tests in $d"
   cd $d
   for test in *.xsh ; do
     
     echo Running test $test
     ../run_test.xsh $test || { echo failed test $test ; exit 1 ; }
   done
   cd ..
done

         
