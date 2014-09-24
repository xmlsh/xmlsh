# test deleting individual files
. ../init
ml:createdir /test/
ml:put -uri /test/test1.xml <[ <foo/> ]>
ml:put -uri /test/test2.xml <[ <bar/> ]>
echo Created 2 files
ml:list /test/
ml:del /test/test1.xml /test/test2.xml
echo after deleting
ml:list /test/
ml:deldir /test/



