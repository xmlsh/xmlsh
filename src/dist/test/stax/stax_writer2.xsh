import commands stax=stax



w=stax:newStreamWriter()
stax:writeStartDocument $w
## In saxon HE prior to 9.6 the following used to work
## without the write Namespace call for { http://www.xmlsh.org/test1 }
stax:writeNamespace $w xmlsh http://www.xmlsh.org/test1
## That was a bug in saxon as well as this test

stax:writeNamespace $w xmlsh2 http://www.xmlsh.org/test2
stax:writeStartElement $w QName( xmlsh http://www.xmlsh.org/test1 test ) 
stax:writeNamespace $w xmlsh2 http://www.xmlsh.org/test2
stax:writeAttribute $w QName( xmlsh2 http://www.xmlsh.org/test2 a1) value1
stax:writeCharacters $w "Some Characters"
stax:writeComment $w "This is a comment"
stax:writeStartElement $w inner 
stax:writeCData $w "This is CData"
stax:writeEndElement $w
stax:writeEndElement $w
stax:writeEndDocument $w


stax:closeWriter $w




