#<p:declare-step type="p:escape-markup">
#     <p:input port="source"/>
#     <p:output port="result"/>
#     <p:option name="cdata-section-elements" select="''"/>         <!-- ListOfQNames -->
#     <p:option name="doctype-public"/>                             <!-- string -->
#     <p:option name="doctype-system"/>                             <!-- anyURI -->
#    <p:option name="escape-uri-attributes" select="'false'"/>     <!-- boolean -->
#     <p:option name="include-content-type" select="'true'"/>       <!-- boolean -->
#     <p:option name="indent" select="'false'"/>                    <!-- boolean -->
#     <p:option name="media-type"/>                                 <!-- string -->
#     <p:option name="method" select="'xml'"/>                      <!-- QName -->
#     <p:option name="omit-xml-declaration" select="'true'"/>       <!-- boolean -->
#    <p:option name="standalone" select="'omit'"/>                 <!-- "true" | "false" | "omit" -->
#     <p:option name="undeclare-prefixes"/>                         <!-- boolean -->
#     <p:option name="version" select="'1.0'"/>                     <!-- string -->
#</p:declare-step>
set +indent
xread _in 
child=$(xecho <[ $_in/node()/node() ]> )
xecho <[ element { node-name( $_in/node() ) } { $_in/node()/@* , $child } ]>
