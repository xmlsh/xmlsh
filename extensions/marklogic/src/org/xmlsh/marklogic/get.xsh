# delete uri ...
for uri ; do
   :query -q "doc(\"$uri\")"
done