# Test of xmd5sum
_D=$PWD
_DIR=$TMPDIR/_xmlsh_md5
[ -d $_DIR ] && rm -rf $_DIR
mkdir -p $_DIR
cp -r ../../samples/data $_DIR
cd $_DIR || exit 1 ;
rm -rf data/.svn

xmd5sum data

cd $_D
rm -rf $_DIR
