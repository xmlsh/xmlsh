# test of xidentity
[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
xtee  $TMPDIR/_xmlsh/a.xml $TMPDIR/_xmlsh/b.xml < ../../samples/data/books.xml

xcmp -x  ../../samples/data/books.xml $TMPDIR/_xmlsh/a.xml || { echo compare failed ; exit 1 ; }

xcmp -x  ../../samples/data/books.xml $TMPDIR/_xmlsh/b.xml || { echo compare failed ; exit 1 ; }

rm -rf $TMPDIR/_xmlsh