# test of xurlencode

xurlencode "a string & with < weird =? chars"  "second&second"
xurlencode -q "first" "arg1" "sec&end" "arg 2"
xurlencode -q <[ "a" , 1 , "b=2" , "spam&amp;bletch" ]>