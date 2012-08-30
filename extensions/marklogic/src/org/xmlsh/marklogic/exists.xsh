# return true if document exists else return false
_opts=$<(xgetopts -a -p "c=connect:" -ps -- "$@")
shift $?

:query $_opts -b -q <{{
xquery version "1.0-ml";
declare variable $uri external ;  
exists(doc($uri))
}}> -v uri $1