# test listing directories 
. ../init
MLCONNECT=$MLCONNECT_SSL
echo Using SSL connection $MLCONNECT
ml:createdir /test/dir1/ /test/dir2/ /test/dir3/
ml:listdir /test/
ml:deldir /test/


