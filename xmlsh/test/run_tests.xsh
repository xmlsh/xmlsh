# Runs all tests 
# checking for required externals


# check for missing TMPDIR
[ -n "$TMPDIR" ] || { echo TMPDIR must be set to run tests ; exit 1 ; }
# expand to java cannonical form and check for directory
TMPDIR=$(xfile $(xfile -c $TMPDIR))
[ -d "$TMPDIR" ] || { echo TMPDIR must be a directory: $TMPDIR ; exit 1 ; }


# Use internal posix module
import commands posix

passed=<[ 0 ]>
failed=<[ 0 ]>

EXTERNS=<["ls","touch","rm","cat","sleep","chmod"]>
echo Some of these tests require access to the internet
echo only files from http://test.xmlsh.org are accessed
echo Checking for required external tools $EXTERNS
# echo in path $PATH
if ! xwhich -n $EXTERNS ; then
	echo Required external programs not found
	exit 1
fi

# Extra tests which depend on local environment
EXTRA=$*

for d in core builtin internal posix java xs stax $EXTRA; do
   echo "running tests in $d"
   cd $d
   for test in *.xsh ; do
     
     echo Running test $test
     # run test 
     ../run_test.xsh $test 
     if [ $? -ne 0 ] ; then	
     	echo failed test $test ;
     	failed=<[  $failed + 1 ]>
     else
     	passed=<[ $passed + 1 ]>
     fi
   done
   cd ..
done

echo Passed: <[ $passed ]>
echo Failed: <[ $failed ]>
         
