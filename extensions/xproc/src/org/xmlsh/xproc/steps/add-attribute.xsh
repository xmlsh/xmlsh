# add-attribute
# <p:declare-step type="p:add-attribute">
#     <p:input port="source"/>
#     <p:output port="result"/>
#     <p:option name="match" required="true"/>                      <!-- XSLTMatchPattern -->
#     <p:option name="attribute-name" required="true"/>             <!-- QName -->
#     <p:option name="attribute-value" required="true"/>            <!-- string -->
# </p:declare-step>

_OPTS=$<(xgetopts match:,attribute-name:,attribute-value: $*)

xed -a <[ attribute { $_OPTS//option[@name="attribute-name"]/value[1] }
                    { $_OPTS//option[@name="attribute-value"]/value[1] } ]> -matches <[ $_OPTS//option[@name="match"]/string() ]>
                    
                    



