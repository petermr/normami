<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:svg="http://www.w3.org/2000/svg">
	<!-- creates templates from parameters (e.g. in projections.xml) -->

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

<!--
<xsl:variable name="external-doc" select="@href"/>
	<xsl:variable name="projections" select="document('${directory}/projections.xml')"/>
  -->

<!--  operates on projections.xml to create template.xml  
<projections cTree="PMC6240665" imageDir="image.6.1.114_482.90_331" basename="raw_s4_thr_150_ds">
 <xcoords>
  <xcoord min="699" max="700"/>
 </xcoords>
 <ycoords>
  <ycoord min="949" max="950"/>
 </ycoords>
 <g class="verticallines" xmlns="http://www.w3.org/2000/svg">
  <line x1="699.0" y1="264.0" x2="699.0" y2="965.0" style="stroke:blue;stroke-width:2.0;"/>
 </g>
 <g class="horizontallines" xmlns="http://www.w3.org/2000/svg">
  <line x1="5.0" y1="949.0" x2="1523.0" y2="949.0" style="stroke:red;stroke-width:2.0;"/>
 </g>
</projections>
-->
<xsl:variable name="ylines">twolines</xsl:variable>

<xsl:variable name="xcoords" select="projections/xcoords"/>
<xsl:variable name="ycoords" select="projections/ycoords"/>
<xsl:variable name="subImage" select="projections/subImage"/>
<xsl:variable name="horizontallines" select="projections/svg:g[@class='horizontallines']"/>
<xsl:variable name="verticallines" select="projections/svg:g[@class='verticallines']"/>

<xsl:variable name="ylines">twolines</xsl:variable>

<!--  the vertical line is probably the best indicator of Y coord  -->
<!--  except it has a tick below it so is too long -->
<!-- 
    <xsl:variable name="y1" select="$verticallines[1]/svg:line/@y1"/>
    <xsl:variable name="y2" select="$verticallines[1]/svg:line/@y2"/>
-->    
 <!-- this is used if there are good horizontal lines -->
    <xsl:variable name="y1" select="$ycoords/ycoord[1]"/>
    <xsl:variable name="y2" select="$ycoords/ycoord[2]"/>
  
<xsl:variable name="sx1" select="$subImage/x[1]"/>
<xsl:variable name="sx2" select="$subImage/x[2]"/>
<xsl:variable name="sx3" select="$subImage/x[3]"/>

<xsl:variable name="bodyTop" select="$y1 + 1"/>
<xsl:variable name="scaleTop" select="$y2 + 1"/>
<!-- 
<xsl:variable name="scaleTop" select="$y2/@max + 1"/>
-->
<xsl:variable name="graphLeft" select="$sx1/@max"/>
<xsl:variable name="graphRight" select="$sx3/@max"/>

	<xsl:template match="/">
	  <xsl:call-template name="createTemplate"/>
	  
	</xsl:template>

	<xsl:template name="createTemplate">
	  <template>
	  <message>y1[<xsl:value-of select="$y1"/>] y2[<xsl:value-of select="$y2"/>] gl[<xsl:value-of select="$graphLeft"/>] gr[<xsl:value-of select="$graphRight"/>]</message>
		  	<image source="raw.png" split="horizontal" sections="header body scale" borders="{$bodyTop} {$scaleTop}" extension="png">
	    	  <image source="raw.body.png" split="vertical" sections="ltable graph rtable" borders="{$graphLeft} {$graphRight}" extension="png">
	    	  </image>
		   </image>
      </template>
	</xsl:template>
	
</xsl:stylesheet>