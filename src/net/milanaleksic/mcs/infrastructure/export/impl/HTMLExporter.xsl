<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" />

    <xsl:variable name="metaData" select="/source/metaData" />

    <xsl:variable name="data" select="/source/data" />

    <xsl:template match="/">
        <html>
            <xsl:comment>
                Ова страница је аутоматски генерисана од стране MCS v<xsl:value-of select="$metaData/version" /> (MovieCatalogSystem) софтвера
                (C) 2007-2012 by milan.aleksic@gmail.com
            </xsl:comment>
            <head>
                <title>Каталог филмова (креирано у MCS v<xsl:value-of select="$metaData/version" />)</title>
                <meta charset="utf-8" />
                <meta name="author" content="Milan Aleksić" />
                <xsl:call-template name="includeCSS" />
                <xsl:call-template name="includeJS" />
            </head>
            <body onload="javascript:init()">
                <h3>Списак филмова у MCS v<xsl:value-of select="$metaData/version" /> бази</h3>
                <small style="text-align:right"><em>timestamp: <xsl:value-of select="$metaData/date" /></em></small>
                <xsl:call-template name="outputTable" />
                <div id="selectionWrapper" style="display:none"><hr /><h3>Листа изабраних филмова</h3>
                <textarea cols="60" rows="10" id="selectionTarget"><!--0--></textarea>
                <input type="button" value="Ресетуј листу" onclick="javascript:resetujListu()" /></div>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="outputTable">
        <table style="width: 100%">
            <tr style="background-color: #CDD4FF; font-weight: bold">
                <xsl:for-each select="$metaData/columns/*">
                    <td>
                        <xsl:if test="position() = 1">
                            <xsl:attribute name="width">80</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="text()" />
                    </td>
                </xsl:for-each>
            </tr>
            <xsl:for-each select="$data/row">
                <tr>
                    <xsl:choose>
                        <xsl:when test="position() mod 2 = 0">
                            <xsl:attribute name="class">r2</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="class">r1</xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:attribute name="id">
                        <xsl:text>f</xsl:text>
                        <xsl:value-of select="position()" />
                    </xsl:attribute>
                    <xsl:for-each select="col">
                        <td>
                            <xsl:value-of select="text()" />
                        </td>
                    </xsl:for-each>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template name="includeCSS">
        <style>
            body { font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 12px; }
            td { padding: 5px; }
            .r1 { background-color:#E5EFC4; cursor:pointer; }
            .r2 { background-color:#FFFFFF; cursor:pointer; }
        </style>
    </xsl:template>

    <xsl:template name="includeJS">
        <script language="javascript"><xsl:comment>
            var singleNext=navigator.userAgent.indexOf("MSIE")>-1 || navigator.userAgent.indexOf("Mozilla/4")>-1 || navigator.userAgent.indexOf("Opera")>-1	;

            function nextElem(elem) {
                if (elem==null)
                    return null;
                var nextElem = null;
                if (singleNext)
                    nextElem = elem.nextSibling;
                else
                    nextElem = elem.nextSibling.nextSibling;
                return nextElem;
            }

            function firstElem() {
                return document.getElementById("f1");
            }

            function isElemSelected(elem) {
                return elem.style.fontWeight == "bold";
            }

            function elemId(elem) {
                return elem.id.substring(1);
            }

            function init() {
                var elem = firstElem();
                while (elem != null) {
                    elem.onclick = handleKlik;
                    var child = elem.firstChild;
                    while (child) {
                        child.onmouseover = handleMouseOver;
                        child.onmouseout = handleMouseOut;
                        child = child.nextSibling;
                    }
                    elem = nextElem(elem);
                }
            }

            function handleMouseOver() {
                this.parentNode.style.backgroundColor = "#d7e18a";
            }

            function handleMouseOut() {
                var parent = this.parentNode;
                if (isElemSelected(parent)) {
                    parent.style.backgroundColor = "lightblue";
                }
                else {
                    var currId = parent.id.substring(1);
                    if (currId % 2 == 1)
                        parent.style.backgroundColor = "#E5EFC4";
                    else
                        parent.style.backgroundColor = "#FFFFFF";
                }
            }

            function handleKlik() {
                if (!isElemSelected(this)) {
                    this.style.backgroundColor = "lightblue";
                    this.style.fontWeight="bold";
                }
                else {
                    var currId = this.id.substring(1);
                    if (currId % 2 == 1)
                        this.style.backgroundColor = "#E5EFC4";
                    else
                        this.style.backgroundColor = "#FFFFFF";
                    this.style.fontWeight="normal";
                }
                refreshChoices();
            }

            function refreshChoices() {
                var text = "";
                var elem = firstElem();
                while (elem != null) {
                    if (isElemSelected(elem)) {
                        var sel = elem.firstChild.nextSibling.innerHTML + " - [" + elem.firstChild.nextSibling.nextSibling.nextSibling.innerHTML + "]";
                        text = text + sel + '\n';
                    }
                    elem = nextElem(elem);
                }
                var selectionWrapper = document.getElementById("selectionWrapper");
                document.getElementById("selectionTarget").value = text;
                if (text.length==0 &amp;&amp; selectionWrapper.style.display.indexOf("block")>-1)
                    selectionWrapper.style.display = "none";
                else if (text.length >0 &amp;&amp; selectionWrapper.style.display.indexOf("none")>-1)
                    selectionWrapper.style.display = "block";
            }

            function resetujListu() {
                var elem = firstElem();
                while (elem != null) {
                    if (elemId(elem)%2==0)
                        elem.style.backgroundColor = "#E5EFC4";
                    else
                        elem.style.backgroundColor = "#FFFFFF";
                    elem.style.fontWeight="normal";
                    elem = nextElem(elem);
                }
                refreshChoices();
            }
        </xsl:comment></script>
    </xsl:template>

</xsl:stylesheet>