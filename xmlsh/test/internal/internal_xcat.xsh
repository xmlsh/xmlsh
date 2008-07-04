# internal_xcat.xsh
# test of xcat


cd $_TEMP
echo <[<foo><x>bar</x></foo>]> > x1.xml
echo <[<foo><x>spam</x></foo>]> > x2.xml

echo xcat files only
xcat x1.xml x2.xml
echo
echo xcat text element 
xcat -w test x1.xml x2.xml
echo
echo xcat xml element
xcat -w <[<test/>]> x1.xml x2.xml
echo



