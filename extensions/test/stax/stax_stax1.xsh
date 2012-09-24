import commands stax=stax


r=stax:newEventReader(../../samples/data/books.xml)


while [ stax:hasNext($r) ] 
do
	
	e=stax:nextEvent($r)
	type=stax:getEventType($e)
	echo event type $type

	case	$type in
		START_ELEMENT ) 	echo "  " stax:getName($e) ;
			if [ stax:getName($e) = "ITEM" ] ; then
				a=stax:getAttribute($e  "CAT")
				xtype $a
				echo CAT is a.getValue()
			fi
			;;
		CHARACTERS    ) echo " " stax:getData($e) ;;
	esac
	
done

stax:closeReader $r 


