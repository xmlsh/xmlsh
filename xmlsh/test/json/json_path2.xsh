import commands json=json

txt=$(<../../samples/data/log.json)
j=json:json( {$txt} )
echo json:path( {$j} Message ) >{xt}
xread -parse x <{xt}
echo <[ $x//title ]>
