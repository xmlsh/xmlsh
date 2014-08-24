# ls

_opts=$<(xgetopts -a -p "c=connect:" -ps -- "$@")
shift $?

:query -v $_opts -q <{{
xquery version "1.0-ml";
declare variable $root external;
for $p in fn:distinct-values( 
    for $d in cts:uris($root,"document", 
          if( $root eq "" ) then () else cts:directory-query($root,"infinity"))
    let $p := substring-after( $d , $root )
    where $d ne $root 
    return 
       if( contains($p,"/") ) then fn:concat($root , substring-before( $p , "/" ) , "/" )
    else 
       concat($root ,$p) 
    )
where $p eq "" or $p ne $root
return $p 
}}> root "$1"