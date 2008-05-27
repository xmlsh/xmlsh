# Runs all tests 
for d in core builtin; do
   echo "running tests in $d"
   cd $d
   for test in *.xsh ; do
     
     echo Running test $test
     ../run_test.xsh $test || exit 1
   done
   cd ..
done


         