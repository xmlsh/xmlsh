# test of get

ml:put -uri /test.xml <[
<foo>
  <bar>string</bar>
</foo>
]>  

ml:rename /test.xml /foo.xml
ml:get /foo.xml
ml:del /foo.xml