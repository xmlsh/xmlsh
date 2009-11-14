# test of xproperties

[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# create a initial properties files in text form
xproperties -text -a var1="Variable 1" -a var2="Variable 2" -a var3="" > xsh.txt

# convert the text form to xml form
xproperties -in xsh.txt -xml > xsh.xml

# delete a property and display as xml with a comment
xproperties -inxml xsh.xml -comment "A comment" -xml -d var2




