# Validate schematron
# schematron schema file
DSDL=$(xuri -r /org/xmlsh/commands/internal/schematron/iso_dsdl_include.xsl)
ABS=$(xuri -r /org/xmlsh/commands/internal/schematron/iso_abstract_expand.xsl)
SVRL=$(xuri -r /org/xmlsh/commands/internal/schematron/iso_svrl_for_xslt2.xsl)

set +omit-xml-declaration
xslt -f $DSDL -i $1 |
xslt -f $ABS | 
xslt -f $SVRL |
xslt -f - -i $2 
