# Test of variable paramater list
. ../common 

null=null()
e= 
a=[]
s=()
m={}
p=""
echo loc() Should be 4
args null one two three
echo loc()Should be 2 '[] {}'
args $null $e $a $m $s $p

echo loc() Should be 4
args {$null} {$e} {$a} {$m} {$s} {$p}

echo loc() Should be 4
: args(null one two three)
echo loc() Should be 2 
: args($null $e $a $m $s $p)
echo loc() Should be 6
: args {$null} {$e} {$a} {$m} {$s} {$p}
echo loc() Should be 4 
: args(null,one,two,three)
echo loc() Should be 3 
: args(null one,,three)
echo loc() Should be 5 
: args(null,one,,three,)
echo loc() Should be 0
: args()
echo loc() Should be 4
: args($null $e $a,$m,$s,$p)
echo loc()  Should be 4
: args({$null} {$e} {$a},{$m},{$s},{$p})
echo loc() Should be 4
: args({$null},{$a},,{$s})
echo loc() Should be 5
: args({$null},{$a},,{$s},)
echo loc() Should be 4
: args(${null} ${e} ${a},${m},${s},${p})
echo loc() Should be 4
: args(${null},${a},,${s})
echo loc() Should be 5
: args(${null},${a},,${s},)
