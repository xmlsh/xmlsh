<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:saxon="http://saxon.sf.net/">

<!-- This stylesheet is used by the XSLTS test suite driver on .NET for comparing actual results
     of a test with the expected ("gold") results. The test specification requires that the results
     should be serialized and canonicalized; comparing the two trees using saxon;deep-equal with the
     flags as set here is a very close approximation -->
     
<!-- This stylesheet needs to be installed in a subdirectory SaxonResults.net within the TestSuiteStagingArea
     directory -->     

<xsl:param name="actual" required="yes" as="document-node()"/>
<xsl:param name="gold" required="yes" as="document-node()"/>
<xsl:param name="debug" select="'false'"/>

<xsl:template name="compare">
<xsl:value-of select="saxon:deep-equal($actual, $gold, (), if ($debug='yes') then 'JNCPS?!' else 'JNCPS')"/>
</xsl:template>

<xsl:output method="text"/>
       
      
    
</xsl:stylesheet>
