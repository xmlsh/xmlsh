# delete uri ...
for uri ; do
   :query -q "xdmp:document-delete(\"$uri\")"
done