# simple test of validate
for i in *.xsh ; do
  validate $i &&  echo Succeeded validation $i || echo Failed validation $i 
done
echo This should fail
validate ../../samples/scripts/invalid1.xsh 2>/dev/null && \
   echo Failed validation did not detect invalid script || \
   echo Success - validate detected invalid script 