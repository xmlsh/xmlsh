# invoke test

# Create a simple xquery file in /Modules/test.xquery 
. ../init
echo '1+1' | ml:put -t -uri /Modules/test.xquery
# ml:set-permissions -x $ML_ROLE -u $ML_ROLE -r $ML_ROLE /Modules/test.xquery
echo 1+1 is $(ml:invoke test.xquery)
ml:deldir /Modules/
