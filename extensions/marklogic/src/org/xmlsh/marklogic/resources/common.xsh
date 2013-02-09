# getopt( $_options optname global default ). 
function getopt()
{

   local _opts=$1
   local _optname=$2
   local _glob=$3
   local _def=$4
   return <[ ($_opts//option[@name eq $_optname]/string(),$_glob,$_def)[1] ]>

} 