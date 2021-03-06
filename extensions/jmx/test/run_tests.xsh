# Runs all tests 
# checking for required externals


# check for missing TMPDIR
[ -n "$TMPDIR" ] || { echo TMPDIR must be set to run tests ; exit 1 ; }
# expand to java cannonical form and check for directory
TMPDIR=$(xfile $(xfile -c $TMPDIR))
[ -d "$TMPDIR" ] || { echo TMPDIR must be a directory: $TMPDIR ; exit 1 ; }


# Use internal posix module
import commands posix
rm -rf $TMPDIR/extensions
mkdir -p $TMPDIR/extensions/jmx
cp ../bin/*.* $TMPDIR/extensions/jmx
#cp ../lib/*.* $TMPDIR/extensions/jmx
XMODPATH=$TMPDIR/extensions
import module jmx=jmx


. init

# Test for empty DB ... do NOT run tests if connection contains ANY documents

ml:query 'count(doc())' >{_NDOCS}
if [ $_NDOCS -ne 0 ] ; then 
	echo Warning these tests will wipe out any existing data
	echo They MUST be run using a connection to an EMPTY database
	echo Your database contains $_NDOCS documents
	echo Test Aborted
	exit 1;
fi

unset _NDOCS

passed=<[ 0 ]>
failed=<[ 0 ]>


# Extra tests which depend on local environment
EXTRA=$*

for d in core $EXTRA; do
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
         
