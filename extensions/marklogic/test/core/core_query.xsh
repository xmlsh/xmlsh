# test basic query 
. ../init

xecho <[ 
   <foo>
      <elem name="a">data a</elem>
      <elem name="b">data b</elem>
      <elem name="c">data c</elem>
   </foo>
]> | ml:put -uri /test_query.xml 

ml:query -q <{{
   declare variable $name external ;
   doc("/test_query.xml")//elem[@name=$name]
 }}> -v name "b" 
 
 # Test exists
 ml:exists /test_query.xml || echo FAIL /test_query.xml does not exist 
 
 ml:del /test_query.xml
 
 
# Test boolean exit
ml:query -b 'fn:true()' && echo Success fn:true
ml:query -b 'fn:false()' || echo Success fn:false 
