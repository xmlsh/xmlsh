declare variable $start external := 1 ;
declare variable $end  external := 1000;
declare variable $urimatch external := "" ;


fn:distinct-values( 
    for $d in cts:uris("","document"  )
    where ($urimatch eq "" or contains( $d , $urimatch ) )
    
    return 
       if( matches( $d , "^[a-zA-Z]+://" ) ) then
          replace( $d , "(^[a-zA-Z]+://[a-zA-Z0-9_.-]+/).*","$1" )
       else
       if( contains( $d , "/" ) ) then 
          substring-before( $d , "/" ) || "/"
       else 
        $d
    
    )[ $start to $end ]