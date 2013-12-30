declare variable $url external ; 
if( ends-with( $url , '/' ) ) then 
   xdmp:directory-create($url) 
else
   ()