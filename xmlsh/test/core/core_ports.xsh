# Test ports
unset doc doc2 doc3 xdoc
# Input XML ports
xread xdoc < ../../samples/data/books.xml
echo There are $(xpath 'count(//ITEM)' <{xdoc}) ITEMS

# Text Input , Text output, external comma 
doc=$(<../../samples/data/books.csv)
cat <{doc} >{doc2}

# Text input xml output
csv2xml <{doc2} >{doc3}

xquery '/root/row[2]/col[3]/string()' <{doc3}

xquery -i $doc3 '/root/row[2]/col[3]/string()'

unset doc doc2 doc3 xdoc

# Test sequences stored into variables
xpath -n "1,2,'foo',3" >{_seq}
echo <[ $_seq[3] ]>

xquery -n "<foo/>,<bar><spam>bletch</spam></bar>,3" >{_seq}
echo <[ $_seq[2]//spam ]>



