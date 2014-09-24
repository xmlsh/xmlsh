# test of xdelattribute - complex case with namespaces and duplicate attributes

declare namespace a=http://www.xmlsh.org/a
declare namespace b=http://www.xmlsh.org/b

xml=<[
<a:list b:attr="battr">
  <a:item a:attr="attra" b:attr="attrb"/>
  <a:item2 a:attr="attra" b:attr2="attrb"/>

  <b:item b:attr="battra" attr="attrb"/>
  <a:item b:attr="battra" attr="attrb"/>
  <a:item attr="attra" b:attr="attrb"/>
  <a:notanitem a:attr="attra" b:attr="attrb"/>
  <notanitem a:attr="attra" b:attr="attrb" b:attr2="attrib2" />
</a:list>
]>

# Test delete a:attr and b:attr2 using either QName constructor or strings
# But only on a:item or notanitem
xdelattribute -a QName(a:attr) -a b:attr2 -e "{http://www.xmlsh.org/a}item" -e notanitem <{xml}
  

