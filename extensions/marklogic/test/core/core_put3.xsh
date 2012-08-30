# 
# Test various options
# Note: this is a minimum test to make sure that atleast the options dont break anything

. ../init
ml:put -quality 10 -uri /test1.xml <[ <foo/> ]>
ml:put +resolve  -uri /test2.xml <[ <foo>&quot;</foo> ]>
ml:put -buffer 10  -uri /test3.xml <[ <foo/> ]>
ml:put -language en  -uri /test3.xml <[ <foo/> ]>
ml:put -locale en_US  -uri /test3.xml <[ <foo/> ]>


# Should be 10
ml:query -q <{{
    xquery version "1.0-ml";
    xdmp:document-get-quality('/test1.xml')
    }}>
# ML BUG
# ml:get -t /test2.xml

ml:deldir /



