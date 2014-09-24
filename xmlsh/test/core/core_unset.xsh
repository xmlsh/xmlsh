# Test local and unset of vars

a="global"
b="global"


function f()
{
  local n=$1
  echo $n a $a 
  echo $n b $b 
  local a;
  a=$*
  echo $n a $a
  unset a
  echo $n a $a
  echo $n b $b
  b="set in f()"
  echo $n b $b
  unset b
  echo $n b $b
  shift 
  if [ $# -gt 0 ] ; then 
    a="a from $n"
    echo calling f from $n : $*
    f $*
    echo retured f $*
    echo $n a $a
    echo $n b $b
   fi
 
   
}

echo a $a
echo b $b
f x y z
echo a $a
echo b $b

  

