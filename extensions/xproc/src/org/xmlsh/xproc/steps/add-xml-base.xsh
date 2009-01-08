# add-xml-base
#<p:declare-step type="p:add-xml-base">
#     <p:input port="source"/>
#     <p:output port="result"/>
#     <p:option name="all" select="'false'"/>                       <!-- boolean -->
#     <p:option name="relative" select="'true'"/>                   <!-- boolean -->
#</p:declare-step>

_OPTS=$<(xgetopts relative:,all: $*)
_R=<[ if( $_OPTS//option[@name="relative"]/value = 'false' ) then "" else "-r" ]>
_A=<[ if( $_OPTS//option[@name="all"]/value = 'true' ) then "-a" else ""]>

eval xaddbase $_R $_A

                    
                    



