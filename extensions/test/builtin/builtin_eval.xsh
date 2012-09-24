# Test of eval

# Set a variable
eval '_A=b'
echo _A is $_A

eval 'unset _A'
echo _A is now unset : $_A

# Echo evaled
_A=B eval 'echo _A is $_A'
unset _A

echo Empty Eval
eval ''
eval '#'
eval ' '
echo Empty Done