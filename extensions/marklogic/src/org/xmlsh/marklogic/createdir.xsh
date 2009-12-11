# delete uri ...
for uri ; do
   :query -q "xdmp:directory-create(\"$uri\")"
done