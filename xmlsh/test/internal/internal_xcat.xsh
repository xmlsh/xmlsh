# internal_xcat.xsh
# test of xcat


cd $TMPDIR
echo <[<foo><x>bar</x></foo>]> > x1.xml
echo <[<foo><x>spam</x></foo>]> > x2.xml

echo xcat files only
xcat -r x1.xml x2.xml

echo xcat text element 
xcat -w test -r x1.xml x2.xml
echo xcat xml element
xcat -w <[<test/>]> -r x1.xml x2.xml



