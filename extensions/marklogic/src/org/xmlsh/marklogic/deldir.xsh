# delete uri ...
for uri ; do
   :query -q "xdmp:directory-delete(\"$uri\")"
done