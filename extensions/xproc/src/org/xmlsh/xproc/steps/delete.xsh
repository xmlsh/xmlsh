#p:delete
#<p:declare-step type="p:delete">
#     <p:input port="source"/>
#     <p:output port="result"/>
#     <p:option name="match" required="true"/>                      <!-- XSLTMatchPattern -->
#</p:declare-step>

_OPTS=$<(xgetopts match: $*)

xed -d  -matches <[ $_OPTS//option[@name="match"]/string() ]>
                    
                    



