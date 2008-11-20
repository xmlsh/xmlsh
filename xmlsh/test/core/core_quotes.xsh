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
echo foo" bar "spam
echo foo' bar 'spam

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


