# Test time command
# ignore actual time output but test that it executes the command

time echo			2>/dev/null
time echo foo 		2>/dev/null
time echo foo bar	2>/dev/null