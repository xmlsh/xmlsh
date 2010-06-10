# test deleting individual files
. ../init
ml:createdir /test/dir1/ /test/dir2/ /test/dir3/

ml:put -uri /test/dir1/test1.xml <[ <foo/> ]>
ml:put -uri /test/dir2/test2.xml <[ <bar/> ]>
echo Created 2 files 3 dirs
ml:listdir /test/
ml:list -r /test/
ml:deldir /test/dir1/
echo Deleted dir1
ml:list -r /test/
ml:direxists /test/ || echo Fail - directory /test/ should exist
ml:deldir /test/
ml:listdir /
echo Success




