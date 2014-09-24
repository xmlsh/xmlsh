# XML Documents reusable parsed DOM trees

cd  $XMLSH/samples/data
xread doc < books.xml

echo Number of AUTHORS  <[count($doc//AUTHOR)]>
echo The Eyre Affair author is 
echo <[$doc//ITEM[TITLE='The Eyre Affair']/AUTHOR/text()]>

