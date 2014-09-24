# list
_opts=$<(xgetopts -i "c=connect:" -s -o "r=recurse" -- "$@")
_popts=$<(xgetopts -a -p "c=connect:" -ps -i "r=recurse" -- "$@")
shift $?


:query -q <{{
xquery version "1.0-ml";
let $collections := cts:collections() return  
<root count="{ fn:count($collections) }">  
{  
for $collection in $collections  
return  
  <collection>  
    <name> { $collection } </name>  
    <size> { fn:count(fn:collection($collection)) } </size>  
  </collection>  
}  
</root>  
}}>