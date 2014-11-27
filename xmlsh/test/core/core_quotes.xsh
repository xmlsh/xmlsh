# Tests of basic quote parsing
. ../common
# basic single and double quotes
echo loc() foo bar
echo loc() 'foo'
echo loc() "foo bar"
echo loc() 'foo bar'

echo loc() foo"bar"
echo loc() foo'bar'

echo loc() "foo"bar
echo loc() 'foo'bar

echo loc() foo"bar"spam
set foo" bar "spam
echo loc() $#
echo loc() $*
set foo' bar 'spam
echo loc() $#
echo loc() $*


echo loc() foo"bar'spam'bletch"barf


# variable expansion within quotes
_A=test

echo loc() "$_A"
echo loc() foo"$_A"
echo loc() foo"$_A"bar
echo loc() "$_A"foo


# verify expansion does NOT take place in single quotes
echo loc() '$_A'
echo loc() foo'$_A'
echo loc() foo'$_A'bar

# Tests of backslashes

echo loc() foo\bar
echo loc() foo\\bar
echo loc() foo\\\bar
echo loc() 'foo\bar'
echo loc() 'foo\\bar'
echo loc() 'foo\\\bar'
echo loc() "foo\bar"
echo loc() "foo\\bar"
echo loc() "foo\\\bar"

# Multiline quoting

echo loc() "on a single \
line"

echo loc() 'on a single \
line'

echo loc() "line 1
line 2"

echo loc() 'line 1
line 2'



# Test that we strip quotes off of variable assigments
_A="test"
echo loc() <[ <foo attr="{$_A}"/> ]>

# Quoting inside variables and $*
_A='""'
echo loc() $_A $_A
set $_A $_A
echo loc() $#
echo loc() $*
echo loc() "$*"

# Initial \ or quoted "
echo loc() \"
echo loc() \'\"
echo loc() \''foo bar'\'

# Test $@ "$@" $* "$@" 
set "foo" '""' "bar spam"
echo -n loc()  
args $*
echo -n loc()  
args x$*y
echo -n loc()  
args x $* y
echo -n loc()  
args "x$*y"
echo -n loc()  
args "x $* y"
echo -n loc()
args "$*" 
echo -n loc()
args ${*}
echo -n loc()
args "${*}" 
echo -n loc()
args "$*bletch"
echo -n loc()
args "A$*bletch"
echo -n loc()
args "A${*}bletch"
echo -n loc()
args $@
echo -n loc()
args ${@}
echo -n loc()
args "$@"
echo -n loc()
args "${@}"
echo -n loc()
args "$@bletch"
echo -n loc()
args "x$@bletch"
echo -n loc()
args "x${@}bletch"

set -indent
# Test preserving quotes in XML expressoins
a=<[ <foo bar="spam">"Text Here in Quotes"</foo> ]>
echo loc() $a
xecho $a
echo loc() "$a"
echo loc() "foo${a}bar"

a=<[ <foo bar='spam'>'Text Here in &quot;Quotes&quot;'</foo> ]>
echo loc() $a
xecho $a
echo loc() "$a"
echo loc() "foo${a}bar"


