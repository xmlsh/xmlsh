# Test of builtin test ([) command
#
# nonzero string
[ "string" ] && echo Success nonzero
[ -n "string" ] && echo Success nonzero
[  "" ] && echo Fail -n
[  -n ""  ] && echo Fail -n 

#
# zero string
[ -z "string" ] && echo Fail -z
[  -z ""  ] && echo Success -z 

#
# string equals
[ "abc" = "abc" ] && echo Success = 
[ "abc" = "abd" ] && echo Fail =

# String not equals
[ "abc" != "abd" ] && echo Success !=
[ "abc" != "abc" ] && echo Fail !=

# Integer eq
[ 1 -eq 1 ] && echo Success -eq
[ 123 -eq 123 ] && echo Success -eq
[ 123 -eq 124 ] && echo Fail -eq

# Integer ge
[ 1 -ge 1 ] && echo Success -ge
[ 123 -ge 122 ] && echo Success -ge
[ 123 -ge 124 ] && echo Fail -ge

# Integer gt
[ 2 -gt 1 ] && echo Success -gt
[ 123 -gt 122 ] && echo Success -gt
[ 123 -gt 123 ] && echo Fail -gt

# Integer le
[ 1 -le 1 ] && echo Success -le
[ 123 -le 124 ] && echo Success -le
[ 123 -le 122 ] && echo Fail -le

# Integer lt
[ 1 -lt 2 ] && echo Success -lt
[ 123 -lt 124 ] && echo Success -lt
[ 123 -lt 122 ] && echo Fail -lt


# Integer ne
[ 1 -ne 2 ] && echo Success -ne
[ 123 -ne 124 ] && echo Success -ne
[ 123 -ne 123 ] && echo Fail -ne

# File name equals
[ . -ef . ] && echo Success -ef
[ ././foo.bar -ef foo.bar ] && echo success -ef
[ ././foo.bar -ef foox.bar ] && echo Fail -ef

# File based tests
#
TDIR=$TMPDIR/_xmlsh

rm -rf $TDIR
[ -f $TDIR -o -d $TDIR ] && echo Fail -f -d
mkdir $TDIR
[ -d $TDIR ] && echo Success -d
[ -f $TDIR ] && echo Fail -f
[ -e $TDIR ] || echo Fail -e directory

echo -n > $TDIR/empty
[ -e $TDIR/empty ] && echo Success  -e
[ -z $TDIR/empty ] && echo Succcess -z 
[ -s $TDIR/empty ] && echo Fail -s

echo notempty > $TDIR/notempty
[ -s $TDIR/notempty ] && echo Success -s
[ -z $TDIR/notempty ] && echo Fail -z 

[ -w $TDIR/notempty ] && echo Success -w
chmod a-w $TDIR/notempty

[ -w $TDIR/notempty ] && echo Fail -w

# Test of -x depends on unix 
# [ -x ... ]

# Tests of compound statements
[ \( \) ] && echo Success empty "()" 
[ \( "test" \) ] && echo Success "()"
[ ! \( "test" \) ] && echo Fail !

# test or 
[ -n "test" -o -n "test" ] && echo Success simple -o
[ 1 -eq 1 -o 1 -eq 2 ] && echo success -o
[ 1 -eq 1 -a 2 -eq 2 ] && echo success -a

# test compound
[ -n "test" -a \( 1 -eq 1 \) -o 2 -eq 2 ] && echo Success compound

# Test zero length sequence
[ <[()]> ] && echo failed zero length sequence test

# Test XExprs in boolean mode 

[ <[ fn:true() ]> ]&& echo success xexpr true
[ <[ fn:false() ]> ]&& echo failed xexpr false

[ { <[ (0,0,0) ]>  } ] && echo success sequence

[ -z <[ fn:false() ]> ] && echo success -z xexpr false

[ -z <[ 0 ]> ] && echo success -z xexpr 0
[ -z <[ 1 ]> ] && echo failed -z xexpr 1
[ -n <[fn:true()]> -o -n <[fn:false()]> ] && echo succeed xexpr or

# Test variables in xexprs
a=<[ "string" ]>
[ $a ] && echo success xexpr string 

b=<[ (1,2,3) ]>
[ -n {$b} ] && echo success xexpr var sequence 

# Test URI flags
[ -u foo ] && echo Test URI Failed 
[ -u foo.bar/spam ] && echo Test URI Failed
[ -u http://foo.bar.spam/bletch ] || echo Test URI Failed

exit 0
