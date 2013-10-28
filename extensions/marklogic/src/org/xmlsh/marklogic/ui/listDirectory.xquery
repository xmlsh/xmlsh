declare variable $root external := "";
declare variable $start external := 1 ;
declare variable $end  external := 1000;

(for $p in 
fn:distinct-values( 
    for $d in cts:uris($root,"document", 
          if( $root eq "" ) then () else cts:directory-query($root,"infinity"))
    let $p := substring-after( $d , $root )
    where $d ne $root 
    return 
       if( contains($p,"/") ) then $root ||  substring-before( $p , "/" ) || "/"
    else 
       $root  || $p 
    )
where $p eq "" or $p ne $root
return $p)[ $start to $end ]