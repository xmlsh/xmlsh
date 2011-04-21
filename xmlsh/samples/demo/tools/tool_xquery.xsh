# XQuery sample
echo Book Titles with number of pages
echo
xquery -i $XMLSH/samples/data/books.xml '
  for $item in //ITEM
  return (
      <BOOK title="{$item/TITLE}" pages="{$item/PAGES}"/> , 
       text{"&#x0A;"}
   )
'
