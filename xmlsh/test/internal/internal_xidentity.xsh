# test of xidentity
xidentity < ../../samples/data/books.xml 


# Check that elements pass through unchanged with variable redirection
a=<[ <foo/> ]>
xidentity <{a} >{b}

[ "$a" = "$b" ] || echo Failed identity should preserve plain elements


