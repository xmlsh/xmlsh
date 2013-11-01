xdmp:database-name(xdmp:database()),
for $d in xdmp:databases() 
  return xdmp:database-name($d)