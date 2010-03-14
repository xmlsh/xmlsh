# delete uri ...
_opts=$<(xgetopts -p "c=connect:,t=text" -ps -- "$@")
shift $?

for uri ; do
   :query $_opts -q "xdmp:document-delete(\"$uri\")"
done