declare variable $root external := "";
cts:uris($root,"document", 
          if( $root eq "" ) then () else cts:directory-query($root,"infinity"))
  