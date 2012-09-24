import commands stax=stax



w=stax:newStreamWriter()
stax:writeStartDocument $w
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




