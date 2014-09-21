# Runs all tests 
# checking for required externals



# check for missing TMPDIR
[ -n "$TMPDIR" ] || { echo TMPDIR must be set to run tests ; exit 1 ; }
# expand to java canonical form and check for directory
TMPDIR=$(xfile $(xfile -c $TMPDIR))
[ -d "$TMPDIR" ] || { echo TMPDIR must be a directory: $TMPDIR ; exit 1 ; }


if xwhich -n aws && aws configure get region >/dev/null 2>&1  ; then 
   echo using aws cli configuration 
else
  echo you need to configure aws with 'aws configure'
  aws configure get region
  exit 1
fi


# Use internal posix module
import commands posix
rm -rf $TMPDIR/extensions
mkdir -p $TMPDIR/extensions/aws
cp ../bin/*.* $TMPDIR/extensions/aws
cp ../lib/*.* $TMPDIR/extensions/aws
XMODPATH=$TMPDIR/extensions

. init

passed=<[ 0 ]>
failed=<[ 0 ]>

# Extra tests which depend on local environment
EXTRA=$*

for d in s3 $EXTRA; do
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
         
