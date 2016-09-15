# core_here.xsh
# HERE documents

echo Tabbed
xcat <<EOF
<foo>
  bar
</foo>
EOF

echo Untabbed
# Tab stripping
xcat <<-EOF
	<foo>
	bar
</foo>
EOF

exit 0