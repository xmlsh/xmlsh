#p:string-replacef
#<p:declare-step type="p:string-replace">
#     <p:input port="source"/>
#     <p:output port="result"/>
#     <p:option name="match" required="true"/>                      <!-- XSLTMatchPattern -->
#     <p:option name="replace" required="true"/>                    <!-- XPathExpression -->
#</p:declare-step>

_OPTS=$<(xgetopts match:,replace: $*)

xed -rx <[ $_OPTS//option[@name="replace"]/string() ]>    -matches <[ $_OPTS//option[@name="match"]/string() ]>
                    
                    



