# rename a marklogic document
XQ=$(xuri -r /org/xmlsh/marklogic/resources/move-forest.xquery)

for uri in $* ; do
	echo moving $uri
	:query -f $XQ -v uri "$uri"
done 