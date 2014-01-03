declare variable $root external := "";
declare variable $urimatch external := "" ;

declare function local:uri-query()
{
     if( $root eq "" ) 
          then () 
     else 
          cts:directory-query($root,"infinity")


};

for $d in cts:uris($root,"document", local:uri-query() )
let $p := substring-after( $d , $root )
where ($urimatch eq "" or contains( $p , $urimatch ) )
return $d
 