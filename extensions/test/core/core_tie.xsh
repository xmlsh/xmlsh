# Test of tie 
# simple use of properties

A=$<( xproperties -a foo="foo value" -a bar="bar value" -a a.b="a.b value")
tie A './/entry[@key = $_ ]/string()'

echo ${A:foo}
echo ${A:bar}
echo ${A:a.b}

# Make sure tie survives after assignment
A=$<( xproperties -a a="a value")
echo ${A:a}

# Book example

xread BOOKS < ../../samples/data/books.xml
tie BOOKS './/CATEGORIES[@DESC eq $_]'

xecho ${BOOKS:Miscellaneous categories}


