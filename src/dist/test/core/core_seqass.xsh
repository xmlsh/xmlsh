# Test assigning and appending sequence

a=(1 2 3)
a+=4
a+=(5 6)
echo $a

a=<[ <foo>text</foo> ]>
a+=<[ <nested><element>text</element></nested> , "text" ]>
xecho $a


# Test sequence appending is scoped
a=<[1 to 10]>
(
	a+=<[11 to 20]>
	echo $a
)
echo $a
