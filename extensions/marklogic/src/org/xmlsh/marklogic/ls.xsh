# ls

_opts=$<(xgetopts -a -p "c=connect:" -ps -- "$@")
shift $?

:query -v $_opts -q <{{
declare variable $root external := "";
for $p in 
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
return $p 
}}> root "$1"