import commands stax=stax


r=stax:newEventReader(../../samples/data/books.xml)

while [ stax:hasNext($r START_ELEMENT) ] 
do
	
	e=stax:nextEvent($r)
	type=stax:getEventType($e)
	echo event type $type
	echo stax:getName($e)

	
done


stax:closeReader( $r )



