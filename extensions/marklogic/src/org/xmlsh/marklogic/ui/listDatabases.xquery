xdmp:database-name(xdmp:database()),
for $d in xdmp:databases() 
  let $n := xdmp:database-name($d)
  order by $n
  return $n