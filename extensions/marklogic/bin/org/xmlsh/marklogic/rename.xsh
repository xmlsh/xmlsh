# rename a marklogic document
XQ=$(xuri -r /org/xmlsh/marklogic/resources/rename.xquery)

:query -f $XQ -v src $1  target $2