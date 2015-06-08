#
# Test of xs:integer
import commands xs=xs

null=()
seq=(a 1 <[ <foo/>, 2 , "bar" ]>  )

for b in true false 1 0 yes no nota "" {$null} <[ fn:true() ]> <[ fn:false() ]>  xs:boolean(true) xs:boolean(false) xs:integer(1) {$seq} ; do
   t=$(xtype -v b)
   v=xs:boolean({$b})
   tv=$(xtype -v v)
   echo "value of $b [$v] original type: $t boolean type: $tv" 
done 

