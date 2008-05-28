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

# TBD: File based tests
#


exit 0

