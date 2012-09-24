import commands stax=stax


w=stax:newStreamWriter()
stax:writeStartDocument $w
stax:writeStartElement $w foo
stax:writeAttribute $w a1 value1
stax:writeCharacters $w "Some Characters"
stax:writeEndElement $w
stax:writeEndDocument $w


stax:closeWriter $w




