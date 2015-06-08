# test of tee
[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
tee  $TMPDIR/_xmlsh/a.xml $TMPDIR/_xmlsh/b.xml < ../../samples/data/books.xml

xcmp  ../../samples/data/books.xml $TMPDIR/_xmlsh/a.xml || { echo compare failed ; exit 1 ; }

xcmp  ../../samples/data/books.xml $TMPDIR/_xmlsh/b.xml || { echo compare failed ; exit 1 ; }

rm -rf $TMPDIR/_xmlsh