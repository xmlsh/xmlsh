# test of get

ml:put -uri /test.xml <[
<foo>
  <bar>string</bar>
</foo>
]>  

ml:get /test.xml
ml:del /test.xml