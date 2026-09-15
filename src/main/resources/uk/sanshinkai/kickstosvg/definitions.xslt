<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/svg/defs">
    <xsl:copy-of select="*" />
  </xsl:template>

</xsl:stylesheet>
