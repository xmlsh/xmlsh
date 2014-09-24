# invoke test

# Create a simple xquery file in /Modules/test.xquery 
. ../init
ml:put -t -uri /Modules/test2.xquery <<EOF
declare variable $id as xs:string external ;
<foo>{$id}</foo>
 
EOF
 
# ml:set-permissions -x $ML_ROLE -u $ML_ROLE -r $ML_ROLE /Modules/test.xquery
ml:invoke -v test2.xquery id test
ml:deldir /Modules/
