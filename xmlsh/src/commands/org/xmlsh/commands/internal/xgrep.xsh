# xgrep xpath [xml files]
import commands stax=stax

_opts=$<(xgetopts -s -i "R=recurse" -o "t=text" -- "$@")
_popts=$<(xgetopts -a -p "R=recurse" -o "t=text" -- "$@")
shift $?



EXPR="$1"
shift

_TEXT=<[ exists($_opts//option[@name="t"])  ]>

files=$<(xls $_popts $*) 

if [ ! $_TEXT ] ; then 
	w=stax:newStreamWriter()
	stax:writeStartDocument $w
	stax:writeStartElement $w xgrep
fi 



for f in <[ $files//file ]> ; do
	file=<[ $f/@path/string() ]>
	if xpath -e "$EXPR" < $file ; then 
		if [ $_TEXT ] ; then 
			echo $file 
		else
		 	stax:write $w $f
		fi
	fi
done

if [ ! $_TEXT ] ; then 
	stax:writeEndElement $w
	stax:writeEndDocument $w
	stax:closeWriter $w
fi