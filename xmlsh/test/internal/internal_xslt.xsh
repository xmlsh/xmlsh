# test of xslt command

# test identity 
cd ../../samples/styles


xslt -f identity.xsl <<EOF
<foo>
text
</foo>
EOF

# Identity with XML expression


xslt -f identity.xsl -i <[<foo>text</foo>]>

# test complicated style
# Note: includes a comment field which is timestamped so failes test
# xslt -f books.xsl -i ../data/books.xml


