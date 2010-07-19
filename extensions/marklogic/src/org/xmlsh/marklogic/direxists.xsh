# return true if diretory exists else return false
_opts=$<(xgetopts -a -p "c=connect:" -ps -- "$@")
shift $?

:query $_opts -b -q <{{
declare variable $uri external ;  
exists(xdmp:document-properties($uri)//prop:directory)
}}> -v uri $1