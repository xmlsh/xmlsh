# Test simple modules
. ../common

# Simple script module
XMODPATH=(. ../../samples/scripts $XMODPATH)
import s=simple_module
# test A Test
echo  -n loc()
s:run A Test
echo  -n loc()
s:run2
echo  -n loc()
: s:run2()

A=parent
echo  -n loc()
s:seta AText

echo loc() A is $A
echo  -n loc()
s:setc CText
echo loc() C is $C
unset A
s:seta ATextMore
echo $A
unset C
s:setc CTextMore
echo loc() parent C is $C
echo loc() internal C is s:getc() 
s:run2
echo  -n loc()
: s:run_child( Child text )
C=ParentC
echo  -n loc()
: s:ev( 'echo $C' )
echo loc() child local: s:getlocal()
: s:setlocal( parent local) 
echo loc() child local: s:getlocal()
x=s
echo loc() $x:varf( f1 f1 args )
y=varf
echo loc() $x:$y( f2 f2 args )

