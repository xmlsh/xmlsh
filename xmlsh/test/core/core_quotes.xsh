# Tests of basic quote parsing

# basic single and double quotes
echo foo bar
echo 'foo'
echo "foo bar"
echo 'foo bar'

echo foo"bar"
echo foo'bar'

echo "foo"bar
echo 'foo'bar

echo foo"bar"spam
set foo" bar "spam
echo $#
echo $*
set foo' bar 'spam
echo $#
echo $*


echo foo"bar'spam'bletch"barf


# variable expansion within quotes
_A=test

echo "$_A"
echo foo"$_A"
echo foo"$_A"bar
echo "$_A"foo


# verify expansion does NOT take place in single quotes
echo '$_A'
echo foo'$_A'
echo foo'$_A'bar

# Tests of backslashes

echo foo\bar
echo foo\\bar
echo foo\\\bar
echo 'foo\bar'
echo 'foo\\bar'
echo 'foo\\\bar'
echo "foo\bar"
echo "foo\\bar"
echo "foo\\\bar"

# Multiline quoting

echo "on a single \
line"

echo 'on a single \
line'

echo "line 1
line 2"

echo 'line 1
line 2'



# Test that we strip quotes off of variable assigments
_A="test"
echo <[ <foo attr="{$_A}"/> ]>
