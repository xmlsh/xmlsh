# Test of subshell expressions with here docs
# discovered as part of xproc implementation

(
true
if true
then 
xcat  <<EOF
<foo/>
EOF

else
echo false
fi
)

{
true
if true
then 
xcat  <<EOF
<foo/>
EOF
else
echo false
fi
}


