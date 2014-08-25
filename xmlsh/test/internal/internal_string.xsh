# 
# Test internal boolean functions
. ../common

message  string() should be empty not null
set string()
[ $# -eq 1 ] && message  Pass 1 arg || message  fail $# args
shift
[ -z string() ] && message  Pass true || message  fail true
error 'string() might need to evaluate to nothing like $s'

message string(Hi There) should be Hi There
[ string(Hi There) = "Hi There" ] && message  Pass true || message  fail true

s=string()
message  $s should be empty not null
## set $s
error 'set $s fails for now'
[ $# -eq 1 ] && message  Pass 1 arg || message  fail $# args
s=string(Hi There)

message $s should be Hi There
[ "$s" = "Hi There" ] && message  Pass true || message  fail true
