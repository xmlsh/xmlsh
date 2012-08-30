# 
# Test -collection
#

. ../init
ml:put -uri /test1.xml -collection foo -collection bar <[ <foo>bar</foo> ]>
ml:put -uri /test2.xml -collection foo <[ <foo>bar</foo> ]>
ml:put -uri /test3.xml -collection bar <[ <foo>bar</foo> ]>
ml:put -uri /test4.xml <[ <foo>bar</foo> ]>


echo foo,bar
ml:query -q <{{
    xquery version "1.0-ml";
    for $u in fn:collection(("foo","bar"))/base-uri() order by $u return $u
    }}>
echo foo
ml:query -q <{{
    xquery version "1.0-ml";
    for $u in  fn:collection("foo")/base-uri() order by $u return $u
        }}>
echo bar
ml:query -q <{{
    xquery version "1.0-ml";
    for $u in fn:collection("bar")/base-uri()  order by $u return $u
    }}>
    
echo none
ml:query -q <{{
    xquery version "1.0-ml";
    fn:collection("none")/base-uri()
    }}>





ml:deldir /



