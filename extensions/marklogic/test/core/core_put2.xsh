# 
# Test -collection
#

. ../init
ml:put -uri /test1.xml -collection foo -collection bar <[ <foo>bar</foo> ]>
ml:put -uri /test2.xml -collection foo <[ <foo>bar</foo> ]>
ml:put -uri /test3.xml -collection bar <[ <foo>bar</foo> ]>
ml:put -uri /test4.xml <[ <foo>bar</foo> ]>


echo foo,bar
ml:query 'for $u in fn:collection(("foo","bar"))/base-uri() order by $u return $u'
echo foo
ml:query 'for $u in  fn:collection("foo")/base-uri() order by $u return $u'
echo bar
ml:query 'for $u in fn:collection("bar")/base-uri()  order by $u return $u'
echo none
ml:query 'fn:collection("none")/base-uri()'





ml:deldir /



