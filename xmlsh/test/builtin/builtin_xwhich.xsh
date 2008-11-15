# Test of xwhich 

foo()
{ echo bar ; }

# test of builtin
xwhich echo 
echo

# test of internal
xwhich xfile
echo

# test of function
xwhich foo 
echo

# test of script
F=$<(xwhich builtin_xwhich.xsh)
echo xwhich is <[$F/xwhich/command/@name/string()]>

# test of external
# PATH MAY VARY - only output name
F=$<(xwhich rm)
echo rm is <[$F/xwhich/command/@name/string()]>
