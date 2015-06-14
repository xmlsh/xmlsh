<?xml version="1.0" encoding="utf-8"?>
<schema    
  xmlns="http://purl.oclc.org/dsdl/schematron"  
  queryBinding='xslt2'
  schemaVersion='ISO19757-3'>                  
  <title>Test ISO schematron file. Introduction mode</title>
  


 <pattern>
    <rule context="ITEM">                                  
      <assert test="TITLE">All items have titles</assert>
    </rule>
  </pattern>


</schema>
