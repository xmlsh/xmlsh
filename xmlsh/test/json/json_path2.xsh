import commands json=json
import commands java
txt=$(<../../samples/data/log.json)
j=json:parse( {$txt} )
xt=json:path( {$j} $.Message )
jset -o {$xt} -m textValue -v s
printvar s | xread -parse x
echo <[ $x//title ]>
