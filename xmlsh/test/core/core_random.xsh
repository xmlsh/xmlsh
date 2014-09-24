# Test of $RANDOM
# Note this test is not very good, its hard to predictably test a random output

[ $RANDOM -ge 0 ] && echo Success
[ $RANDOM -ne $RANDOM ] && echo Success
