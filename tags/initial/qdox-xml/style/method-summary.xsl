<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xslt/java"
				version="1.0">
    
<xsl:output method="text" encoding="ascii" />

<xsl:template match="/">
<xsl:apply-templates select="qdox/source/class" />
</xsl:template>

<xsl:template match="class">

CLASS <xsl:value-of select="name" />
<xsl:apply-templates select="method" />
</xsl:template>

<xsl:template match="method">
    METHOD <xsl:value-of select="name" />
<xsl:apply-templates select="parameter" />
</xsl:template>

<xsl:template match="parameter">
        PARAM <xsl:value-of select="name" /> (<xsl:value-of select="type" />)</xsl:template>

</xsl:stylesheet>
