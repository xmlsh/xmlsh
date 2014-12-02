# Runs all tests 
# checking for required externals

MODULES=(core builtin internal posix java json xs stax types modules)

usage()
{
  echo -- usage: $0 [-x|-X] [-t test [-t test ...]] [extra tests...] >(error)
  [ $# -gt 0 ] && echo -- $* >(error)
  exit 1;
}

while [ $# -gt 0 ] ; do
  case $1 in 
  -x) EXIT=1 ;;
  -X) TEST=-test ; EXIT=1 ;; 
  -t) MODULES=() ;;
  -*) usage "Unexpected arg $1" ;;
   *) MODULES+=( $1 ) ;;
 esac
 shift ;
done

# check for missing TMPDIR
[ -n "$TMPDIR" ] || usage TMPDIR must be set to run tests 
# expand to java cannonical form and check for directory
TMPDIR=$(xfile $(xfile -c $TMPDIR))
[ -d "$TMPDIR" ] || usage TMPDIR must be a directory: $TMPDIR 


# Use internal posix module
import commands posix

passed=<[ 0 ]>
failed=<[ 0 ]>

EXTERNS=<["ls","touch","rm","cat","sleep","chmod"]>
echo Some of these tests require access to the internet
echo only files from http://test.xmlsh.org are accessed
echo Checking for required external tools $EXTERNS
# echo in path $PATH
xwhich -n $EXTERNS || usage Required external programs not found

for d in $MODULES ; do 
   echo "running tests in $d"
   cd $d
   for test in *.xsh ; do
     
     echo Running test $test
     # run test 
     ../run_test.xsh $TEST $test 
     if [ $? -ne 0 ] ; then	
     	echo failed test $test ;
     	failed=<[  $failed + 1 ]>
     	[ "$EXIT" = "1" ] && exit 1 ;
     else
     	passed=<[ $passed + 1 ]>
     fi
   done
   cd ..
done

echo Passed: <[ $passed ]>
echo Failed: <[ $failed ]>
         
