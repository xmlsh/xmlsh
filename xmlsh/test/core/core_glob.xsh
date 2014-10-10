. ../common
import commands posix
[ -z "$TMPDIR" -o ! -d "$TMPDIR" ] && die Temp directory required TMPDIR: $TMPDIR

rm -f $TMPDIR/*.core_glob

D=$TMPDIR/test.core_glob
mkdir -p $D || die "Cant make directory $D"
trap 'rm -rf $D' EXIT
cd $D || die "Cant cd to $D"
echo test > test
echo dot-test > .test
echo dotdot-test > ..test
mkdir -p dir || die "cant make directory $D/dir"
mkdir -p .dir ||  die "cant make directory $D/dir"
mkdir -p .dir/.dir2 || die "cant make directory $D/.dir/.dir2"
echo  .dir2.hide2 > .dir/.dir2/.hide2
echo .dir.hide > .dir/.hide
echo .dirsee > .dir/see
echo see > see
echo dirsee > dir/see
echo loc()
ls
echo loc()
ls -a
echo loc()
ls -r
echo loc()
ls -a -r  
echo loc()
ls see
echo loc()
ls dir
echo loc()
ls .dir
echo loc()
ls .dir/.dir2
echo loc()
ls -a .dir/.dir2
echo loc()
echo *
echo loc()
echo .*
echo loc()
echo .dir*
echo loc()
echo */.d*
echo loc()
echo .di*/*
echo loc()
echo .dir/.*
echo loc()
echo .d*/.d*/*
echo loc()
echo .dir/*/*
echo .d*/.*/.*
echo loc()


