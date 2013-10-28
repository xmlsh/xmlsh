declare variable $url external ; 
if( ends-with( $url , '/' ) ) then 
   xdmp:directory-delete($url) 
else 
   xdmp:document-delete($url)
