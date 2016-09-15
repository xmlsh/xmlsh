# Test ports
. ../common
unset doc doc2 doc3 xdoc
# Input XML ports
xread xdoc < ../../samples/data/books.xml
echo loc() There are $(xpath 'count(//ITEM)' <{xdoc}) ITEMS

# Text Input , Text output, external comma 
doc=$(<../../samples/data/books.csv)
cat <{doc} >{doc2}

# Text input xml output
csv2xml <{doc2} >{doc3}

xquery '/root/row[2]/col[3]/string()' <{doc3}

xquery -i $doc3 '/root/row[2]/col[3]/string()'

unset doc doc2 doc3 xdoc

# Test sequences stored into variables
xpath -n "1,2,'foo',3" >{_seq}
echo loc() <[ $_seq[3] ]>

xquery -n "<foo/>,<bar><spam>bletch</spam></bar>,3" >{_seq}
echo loc() There are <[count($_seq)]> elements : should be 3
echo loc() <[ $_seq[2]//spam ]>  

# Test appending xml values to ports 
unset _seq
xpath -n 1 >{_seq}
xquery -n "<foo/>,<bar><spam>bletch</spam></bar>" >>{_seq}
xpath -n 2 >>{_seq}
echo loc() There are <[count($_seq)]> elements : should be 4
echo loc() <[ $_seq[3]//spam ]>


# write and append to text 
unset _seq
echo -n foo >{_var}
echo -n bar >>{_var}
echo loc() $_var should be foobar

# Test mixed string and xml 
echo loc() Should be sequence "foobar" , '<foo/>'
xecho <[ <foo/> ]> >>{_var}
xecho loc() $_var
echo loc() Should be same 
xecho {$_var}
echo loc() should be sequence or array
xtype -s -v _var
echo loc() 'should be string / element()'
xtype -s ${_var}
# Note: this results in a string 

# Reverse the order and 
unset _var

echo loc() test append to an unset var
echo empty >>{_unset}
echo -n full >>{_unset}
xtype -s -v _unset
echo loc() {$_unset}
echo loc() ${#_unset}
unset _var
# Test mixed string and xml 
xecho <[ <foo/> ]> >>{_var}
echo Should be xdm '<foo/>'
xecho $_var    # should be xdm
echo -n foo >>{_var}
echo -n bar >>{_var}
echo loc() should be  3 lines
xecho $_var   # should be xdm as well
echo loc() ${#_var} should be 3
xtype -s {$_var}
echo loc() ${#_var}

# Test brace group into ports
unset _var
{ xecho <[ <foo>brace group</foo> ]>; xecho <[ <bar/> ]>;  } >{_var}
xecho $_var
unset _var
( xecho <[ <foo>sub shell</foo> ]>; xecho <[ <bar/> ]>;  ) >{_var}
xecho $_var
unset _var


# Test named ports (output)

echo loc() testing port "(output)"
echo -p output output 
echo loc() output2 >(output)

for i in output3 ; do 
   echo $i should be "output3" on stdout once
done >(output)

echo output4 |  while read a ; do echo $a ; done >(output)

if true ; then echo output5 ; fi >(output)

echo loc() testing output then break should be output6 once
while true ; do 
  echo output6
  break ;
done >(output)
echo loc() should be output7 then output 8
{ echo output7 ;} >(output)
( echo output8 ) >(output)

echo loc() testing redirect through compound statements
echo  input1 | read a <(input)
echo loc() $a
echo input2 | while read a ; do echo $a ; done <(input)
echo  input3 | if true ; then read a ;  echo $a ; fi <(input)
echo input4 | for a in a ; do read b ; echo $b ; done <(input)
echo loc() done





