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
 ml:del /test_query.xml
 
