# Move documents to a new forest
XQ=$(xuri -r /org/xmlsh/marklogic/resources/document-move-forest.xquery)

for uri in $* ; do
	echo moving $uri
	:query -f $XQ -v uri "$uri"
done 