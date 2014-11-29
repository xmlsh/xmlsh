# Test script execution 
. ../common
import commands posix


if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	die Temp directory required TMPDIR: $TMPDIR
fi

T=$(xfile $TMPDIR xmlsh$$-s)
T1=${T}1.bat

trap  'rm  -f ${T}1 ${T}2 ${T}3 ${T}4'  EXIT

cat >$T1  <<EOF
@REM This is a batch script
message THIS IS A DOS BAT SCRIPT
EOF



T2=${T}2.cmd
cat >$T2 <<EOF
@REM This is a batch script
message THIS IS A DOS BAT SCRIPT
EOF

T3=${T}3.xsh 
cat >$T3  <<EOF
VAR=1
message This is an xsh script
EOF


T4=${T}4
cat >$T4  <<EOF
VAR=2
message This is an xsh script
EOF


message 'Source of $T4 Should Succeed'

. $T4
message VAR is now $VAR
message 'Source of ${T}3 Should Succeed and resolve to $T3'
. ${T}3 || message FAILED

message Should fail to run
. ${T1} 2>/dev/null || message SUCCEED SHOULD FAIL 

message Should fail to run
. ${T2} 2>/dev/null || message SUCCEED SHOULD FAIL

message Should Succeed
. ${T3} && message SUCCEEDED || message FAILED

