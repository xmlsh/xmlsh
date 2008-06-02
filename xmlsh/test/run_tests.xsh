# Runs all tests 
# checking for required externals

EXTERNS=<["pwd","ls","touch","rm","cat","sleep"]>

echo Checking for required external tools $EXTERNS
echo in path $PATH
if ! xwhich -n $EXTERNS ; then
	echo Required external programs not found
	exit 1
fi

# Set up a temp directory
#_TEMP=$PWD/_temp
#mkdir $_TEMP




for d in core builtin; do
   echo "running tests in $d"
   cd $d
   for test in *.xsh ; do
     
     echo Running test $test
     ../run_test.xsh $test || exit 1
   done
   cd ..
done


         