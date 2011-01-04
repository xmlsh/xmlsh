# Test stax writing XML values 

import commands stax=stax


w=stax:newStreamWriter()
stax:writeStartDocument $w
stax:writeStartElement $w foo
stax:write $w <[ <bar>spam</bar> ]>
stax:writeStartElement $w test
stax:write $w Characters 
stax:writeEndElement $w
stax:writeEndElement $w
stax:writeEndDocument $w


stax:closeWriter $w




