# test for xuriu

xuri http://www.xmlsh.org
xuri http://www.xmlsh.org /foo.bar
xuri http //www.xmlsh.org/foo.bar fragment
xuri http www.xmlsh.org /path/foo.bar query fragment
xuri http user www.xmlsh.org 80 /path/foo.bar query fragment
xuri http "" www.xmlsh.org 80 /path/foo.bar query fragment
xuri http "" www.xmlsh.org "" /path/foo.bar query fragment
xuri http "" www.xmlsh.org "" /path/foo.bar "" fragment

uri=$(xuri http user www.xmlsh.org 80 /path/foo.bar query fragment)
xuri -a $uri
xuri -f $uri
xuri -h $uri
xuri -p $uri
xuri -P $uri
xuri -q $uri
xuri -s $uri
