# 
# Test internal boolean functions
. ../common

message  true() should be true
message  false() should be false
[ true() ] && message  Pass true || message  fail true
[ false() ] && message  Fail false || message  Pass false
[ true() = true() ] && message  Pass true || message  fail true
[ false() = true() ] && message  Fail equal || message  Pass unequal
[ true() = false() ] && message  Fail equal || message  Pass unequal

import module b=java:java.lang.Boolean
bt=b:new(true)
bf=b:new(false)
t=true()
f=false()

message  $t should be true
message  $f should be false
message  $bt should be true
message  $bf should be false

[ $t] && message  Pass true || message  fail true
[ $f ] && message  Fail false || message  Pass false
[ $bt ] && message  Pass true || message  fail true
[ $bf ] && message  Fail false || message  Pass false
[ $t = $t ] && message  Pass true || message  fail true
[ $bt = $bt ] && message  Pass true || message  fail true
[ $bt = $t ] && message  Pass true || message  fail true
[ $t = $bt ] && message  Pass true || message  fail true
[ $f = $f ] && message  Pass true || message  fail true
[ $bf = $bf ] && message  Pass true || message  fail true
[ $bf = $f ] && message  Pass true || message  fail true
[ $f = $bf ] && message  Pass true || message  fail true

[ false() = true() ] && message  Fail equal || message  Pass unequal
[ true() = false() ] && message  Fail equal || message  Pass unequal


message  boolean(true) should be true
message  boolean(false) should be false
message  boolean() should be false
message  boolean( "" ) should be false

bt=boolean(true)
bf=boolean(false)
bn=boolean()
bs=boolean( "" )

message  $bt should be true
message  $bf should be false
message  $bn should be false
message  $bs should be false

[ $bt] && message  Pass true || message  fail true
[ $bf ] && message  Fail false || message  Pass false
[ $bn ] && message  Fail false || message  Pass false
[ $bs ] && message  Fail false || message  Pass false

[ $t = $bt ] && message  Pass true || message  fail true
[ $f = $bf ] && message  Pass true || message  fail true
[ $f = $bn ] && message  Pass true || message  fail true
[ $t = $bs ] && message  Pass true || message  fail true




# For later
[ true() = <[ fn:true() ]> ] && message  Pass Good ! || error  Fail for now TODO
[ false() = <[ fn:false() ]> ] && message  Pass Good ! || error Fail for now TODO

