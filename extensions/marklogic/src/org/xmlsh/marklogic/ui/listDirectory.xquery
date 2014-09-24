declare variable $root external := "";
declare variable $start external := 1 ;
declare variable $end  external := 1000;
declare variable $urimatch external := "" ;

declare function local:uri-query()
{
     if( $root eq "" ) 
          then () 
     else 
          cts:directory-query($root,"infinity")


};

(for $p in 
fn:distinct-values( 
    for $d in cts:uris($root,"any", 
         local:uri-query() )
    let $p := substring-after( $d , $root )
    where ( $d ne $root ) and ($urimatch eq "" or contains( $p , $urimatch ) )
    return 
       if( contains($p,"/") ) then $root ||  substring-before( $p , "/" ) || "/"
    else 
       $root  || $p 
    )
where $p eq "" or $p ne $root
return $p)[ $start to $end ]