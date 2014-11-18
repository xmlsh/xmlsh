# Test script execution 
. ../common
import commands posix


if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	die Temp directory required TMPDIR: $TMPDIR
fi

T=$TMPDIR/xmlsh$$-s
T1=${T}1.bat

cat >$T1  <<EOF
@REM This is a batch script
echo THIS IS A DOS BAT SCRIPT
EOF



T2=${T}2.cmd
cat >$T2 <<EOF
@REM This is a batch script
echo THIS IS A DOS BAT SCRIPT
EOF

T3=${T}3.xsh 
cat >$T3  <<EOF
VAR=1
echo This is an xsh script
EOF


T4=${T}4
cat >$T4  <<EOF
VAR=2
echo This is an xsh script
EOF

#ls -l ${T}*
#ead waiting
#trap  'rm $T1 $T2 $T3 $T4'  EXIT


echo loc() "Source of $T4 Should Succeed"

. $T4
echo VAR is now $VAR
echo loc() 'Source of ${T}3 Should Succeed and resolve to $T3'
. ${T}3 || echo FAILED

echo loc() Should fail to run
. ${T1} 2>/dev/null || echo SHOULD FAIL 

echo loc() Should fail to run
. ${T2} 2>/dev/null || echo SHOULD FAIL

echo loc() Should Succeed
. ${T3} && echo SUCCEEDED || echo FAILED


trap  'rm $T1 $T2 $T3 $T4'  EXIT
