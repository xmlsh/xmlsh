import commands json=json

txt=$(<../../samples/data/log.json)
j=json:json( {$txt} )
xt=json:path( {$j} $.Message )
s=xt.textValue()
printvar s | xread -parse x
echo <[ $x//title ]>
