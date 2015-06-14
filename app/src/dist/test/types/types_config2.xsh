# Config modules
. ../common
import c=types.config
import p=types.properties

var1="VARIABLE 1"
readconfig -format ini -file ../../samples/types/sample.config _c

message Testing default resolution to name
message server.name: ${_c[server.name]}
message servers.name: ${_c[servers.name]}
message test.name: ${_c[test.name]}
message name: ${_c[name]}
message Only exists in servers
message server.web: ${_c[server.web]}
message servers.web: ${_c[servers.web]}
message test.web: ${_c[test.web]}
message has key server.name c:has-key( $_c server.name ) 
message has key server,name c:has-key( $_c server name ) 
message has key servers.name c:has-key( $_c servers.name ) 
message has key servers name c:has-key( $_c servers name ) 
message has key test.name c:has-key( $_c test.name ) 
message has key name c:has-key( $_c name ) 
message has key servers c:has-key( $_c servers ) 
message has key nope c:has-key( $_c nope ) 
message The folloiwng is TBD: 
message has key nope.name c:has-key( $_c nope.name )
message But its consistant with the following
message get of key nope.name: c:get( $_c nope.name ) 
message value of key nope.name: c:get-value( $_c nope.name )
message value of key nope name: c:get( $_c nope name )
message get section server - should be blank
message c:section( $_c server )
message get section on servers 
message c:section( $_c servers )
message c:get( $_c servers )

