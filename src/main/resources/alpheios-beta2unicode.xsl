<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:exsl="http://exslt.org/common">
<!-- 
   This file is a part of the source code and related artifacts for the
   Project Bamboo 
   
   For more information please see:
   http://www.projectbamboo.org
   https://wiki.projectbamboo.org
    
   Copyright 2012 
   The Regents of the University of California, Berkeley ("Berkeley") 
   the Australian National University; Indiana University 
   Northwestern University 
   Tufts University
   University of Chicago
   The Board of Trustees of the University of Illinois at Urbana-Champaign 
   University of Maryland 
   University of Oxford
   University of Wisconsin, Madison 
  
   Licensed under the Educational Community License (ECL), Version 2.0.
   You may not use this file except in compliance with this License.
   You may obtain a copy of the ECL 2.0 License at:
   https://source.projectbamboo.org/svn/btp/LICENSE.txt
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Based on code from The Alpheios Project  
   Copyright 2008-2009 Cantus Foundation
   http://alpheios.net
   published under the GNU General Public License
   http://www.gnu.org/licenses/
 -->

  <xsl:output media-type="text/xml" omit-xml-declaration="no" method="xml" indent="no"/>

  <xsl:param name="e_upper" select="false()"/>
  <xsl:param name="e_precomposed" select="true()"/>
  <xsl:param name="e_partial" select="false()"/>
  
  <!-- Upper/lower tables.  Note: J is not a valid betacode base character. -->
  <xsl:variable name="s_betaUppers">ABCDEFGHIKLMNOPQRSTUVWXYZ</xsl:variable>
  <xsl:variable name="s_betaLowers">abcdefghiklmnopqrstuvwxyz</xsl:variable>

  <!-- diacritics in betacode and combining unicode -->
  <xsl:variable name="s_betaDiacritics">()+/\=|_^&apos;</xsl:variable>
  <xsl:variable name="s_uniDiacritics"
    >&#x0314;&#x0313;&#x0308;&#x0301;&#x0300;&#x0342;&#x0345;&#x0304;&#x0306;&#x1FBD;</xsl:variable>

  <!-- characters with and without length diacritics -->
  <xsl:variable name="s_betaWithLength">_^</xsl:variable>
  <xsl:variable name="s_betaWithoutLength"/>
  <xsl:variable name="s_betaWithDiaeresis">+</xsl:variable>
  <xsl:variable name="s_betaWithoutDiaeresis"/>
  <xsl:variable name="s_betaWithCaps">*</xsl:variable>
  <xsl:variable name="s_betaWithoutCaps"/>
  <xsl:variable name="s_uniWithLength"
    >&#x1FB0;&#x1FB1;&#x1FB8;&#x1FB9;&#x1FD0;&#x1FD1;&#x1FD8;&#x1FD9;&#x1FE0;&#x1FE1;&#x1FE8;&#x1FE9;&#x00AF;&#x0304;&#x0306;</xsl:variable>
  <xsl:variable name="s_uniWithoutLength"
    >&#x03B1;&#x03B1;&#x0391;&#x0391;&#x03B9;&#x03B9;&#x0399;&#x0399;&#x03C5;&#x03C5;&#x03A5;&#x03A5;</xsl:variable>
  <xsl:variable name="s_uniWithDiaeresis"
    >&#x0390;&#x03AA;&#x03AB;&#x03B0;&#x03CA;&#x03CB;&#x1FD2;&#x1FD3;&#x1FD7;&#x1FE2;&#x1FE3;&#x1FE7;&#x1FC1;&#x1FED;&#x1FEE;&#x00A8;&#x0308;</xsl:variable>
  <xsl:variable name="s_uniWithoutDiaeresis"
    >&#x03AF;&#x0399;&#x03A5;&#x03CD;&#x03B9;&#x03C5;&#x1F76;&#x1F77;&#x1FD6;&#x1F7A;&#x1F7B;&#x1FE6;&#x1FC0;&#x1FEF;&#x1FFD;</xsl:variable>
  <xsl:variable name="s_uniWithCaps"
    >&#x1F8D;&#x1F0D;&#x1F8B;&#x1F0B;&#x1F8F;&#x1F0F;&#x1F89;&#x1F09;&#x1F8C;&#x1F0C;&#x1F8A;&#x1F0A;&#x1F8E;&#x1F0E;&#x1F88;&#x1F08;&#x0386;&#x1FBA;&#x1FBC;&#x1FB9;&#x1FB8;&#x0391;&#x0392;&#x039E;&#x0394;&#x1F1D;&#x1F1B;&#x1F19;&#x1F1C;&#x1F1A;&#x1F18;&#x0388;&#x1FC8;&#x0395;&#x03A6;&#x0393;&#x1F9D;&#x1F2D;&#x1F9B;&#x1F2B;&#x1F9F;&#x1F2F;&#x1F99;&#x1F29;&#x1F9C;&#x1F2C;&#x1F9A;&#x1F2A;&#x1F9E;&#x1F2E;&#x1F98;&#x1F28;&#x0389;&#x1FCA;&#x1FCC;&#x0397;&#x1F3D;&#x1F3B;&#x1F3F;&#x1F39;&#x1F3C;&#x1F3A;&#x1F3E;&#x1F38;&#x03AA;&#x038A;&#x1FDA;&#x1FD9;&#x1FD8;&#x0399;&#x039A;&#x039B;&#x039C;&#x039D;&#x1F4D;&#x1F4B;&#x1F49;&#x1F4C;&#x1F4A;&#x1F48;&#x038C;&#x1FF8;&#x039F;&#x03A0;&#x0398;&#x1FEC;&#x03A1;&#x03A3;&#x03A4;&#x1F5D;&#x1F5B;&#x1F5F;&#x1F59;&#x03AB;&#x038E;&#x1FEA;&#x1FE9;&#x1FE8;&#x03A5;&#x03DC;&#x1FAD;&#x1F6D;&#x1FAB;&#x1F6B;&#x1FAF;&#x1F6F;&#x1FA9;&#x1F69;&#x1FAC;&#x1F6C;&#x1FAA;&#x1F6A;&#x1FAE;&#x1F6E;&#x1FA8;&#x1F68;&#x038F;&#x1FFA;&#x1FFC;&#x03A9;&#x03A7;&#x03A8;&#x0396;&#x1FBB;&#x1FC9;&#x1FCB;&#x1FDB;&#x1FF9;&#x1FEB;&#x1FFB;</xsl:variable>
  <xsl:variable name="s_uniWithoutCaps"
    >&#x1F85;&#x1F05;&#x1F83;&#x1F03;&#x1F87;&#x1F07;&#x1F81;&#x1F01;&#x1F84;&#x1F04;&#x1F82;&#x1F02;&#x1F86;&#x1F06;&#x1F80;&#x1F00;&#x03AC;&#x1F70;&#x1FB3;&#x1FB1;&#x1FB0;&#x03B1;&#x03B2;&#x03BE;&#x03B4;&#x1F15;&#x1F13;&#x1F11;&#x1F14;&#x1F12;&#x1F10;&#x03AD;&#x1F72;&#x03B5;&#x03C6;&#x03B3;&#x1F95;&#x1F25;&#x1F93;&#x1F23;&#x1F97;&#x1F27;&#x1F91;&#x1F21;&#x1F94;&#x1F24;&#x1F92;&#x1F22;&#x1F96;&#x1F26;&#x1F90;&#x1F20;&#x03AE;&#x1F74;&#x1FC3;&#x03B7;&#x1F35;&#x1F33;&#x1F37;&#x1F31;&#x1F34;&#x1F32;&#x1F36;&#x1F30;&#x03CA;&#x03AF;&#x1F76;&#x1FD1;&#x1FD0;&#x03B9;&#x03BA;&#x03BB;&#x03BC;&#x03BD;&#x1F45;&#x1F43;&#x1F41;&#x1F44;&#x1F42;&#x1F40;&#x03CC;&#x1F78;&#x03BF;&#x03C0;&#x03B8;&#x1FE5;&#x03C1;&#x03C3;&#x03C4;&#x1F55;&#x1F53;&#x1F57;&#x1F51;&#x03CB;&#x03CD;&#x1F7A;&#x1FE1;&#x1FE0;&#x03C5;&#x03DD;&#x1FA5;&#x1F65;&#x1FA3;&#x1F63;&#x1FA7;&#x1F67;&#x1FA1;&#x1F61;&#x1FA4;&#x1F64;&#x1FA2;&#x1F62;&#x1FA6;&#x1F66;&#x1FA0;&#x1F60;&#x03CE;&#x1F7C;&#x1FF3;&#x03C9;&#x03C7;&#x03C8;&#x03B6;&#x1F71;&#x1F73;&#x1F75;&#x1F77;&#x1F79;&#x1F7B;&#x1F7D;</xsl:variable>

  <!-- characters denoting a word separation: punctuation plus whitespace -->
  <xsl:variable name="s_betaSeparators">
    .,:;_&#x0009;&#x000A;&#x000D;&#x0020;&#x0085;&#x00A0;&#x1680;&#x180E;
    &#x2000;&#x2001;&#x2002;&#x2003;&#x2004;&#x2005;&#x2006;&#x2007;&#x2008;&#x2009;&#x200A;
    &#x2028;&#x2029;&#x202F;&#x205F;&#x3000; </xsl:variable>

  <!-- more characters denoting the end of a word -->
  <xsl:variable name="s_betaSeparators2">0123456789[]{}</xsl:variable>

  <!-- keys for lookup table -->
  <xsl:key name="s_betaUniLookup" match="beta-uni-table/entry" use="beta"/>
  <xsl:key name="s_unicBetaLookup" match="beta-uni-table/entry" use="unic"/>

  <!--
    Table mapping betacode sequences to Unicode
    Betacode sequences have the form
      <letter><diacritics>
    where
      - <letter> is one of the base Greek characters as
        represented in betacode
      - <diacritics> is a string in canonical order:
        * = capitalize
        ( = dasia/rough breathing
        ) = psili/smooth breathing
        + = diaeresis
        / = acute accent
        \ = grave accent
        = = perispomeni
        | = ypogegrammeni
        _ = macron [non-standard, Perseus]
        ^ = breve [non-standard, Perseus]
        ' = koronis [non-standard]

    Each entry in the table contains a betacode sequence
    plus the corresponding precomposed Unicode sequence (<unic> element)
    and decomposed Unicode sequence (<unid> element>.
    But for entries for which the <beta> content contains only diacritics,
    <unic> holds the non-combining form, while <unid> holds the combining form.

    To get around tree fragment restrictions in XSLT 1.0, the actual variable
    uses exsl:node-set().
  -->
  <xsl:variable name="s_rawTable">
    <beta-uni-table>
      <entry>
        <beta>a*(/|</beta>
        <unic>&#x1F8D;</unic>
        <unid>&#x0391;&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*(/</beta>
        <unic>&#x1F0D;</unic>
        <unid>&#x0391;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>a*(\|</beta>
        <unic>&#x1F8B;</unic>
        <unid>&#x0391;&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*(\</beta>
        <unic>&#x1F0B;</unic>
        <unid>&#x0391;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>a*(=|</beta>
        <unic>&#x1F8F;</unic>
        <unid>&#x0391;&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*(=</beta>
        <unic>&#x1F0F;</unic>
        <unid>&#x0391;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>a*(|</beta>
        <unic>&#x1F89;</unic>
        <unid>&#x0391;&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*(</beta>
        <unic>&#x1F09;</unic>
        <unid>&#x0391;&#x0314;</unid>
      </entry>
      <entry>
        <beta>a*)/|</beta>
        <unic>&#x1F8C;</unic>
        <unid>&#x0391;&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*)/</beta>
        <unic>&#x1F0C;</unic>
        <unid>&#x0391;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>a*)\|</beta>
        <unic>&#x1F8A;</unic>
        <unid>&#x0391;&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*)\</beta>
        <unic>&#x1F0A;</unic>
        <unid>&#x0391;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>a*)=|</beta>
        <unic>&#x1F8E;</unic>
        <unid>&#x0391;&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*)=</beta>
        <unic>&#x1F0E;</unic>
        <unid>&#x0391;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>a*)|</beta>
        <unic>&#x1F88;</unic>
        <unid>&#x0391;&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*)</beta>
        <unic>&#x1F08;</unic>
        <unid>&#x0391;&#x0313;</unid>
      </entry>
      <entry>
        <beta>a*/</beta>
        <unic>&#x0386;</unic>
        <unid>&#x0391;&#x0301;</unid>
      </entry>
      <entry>
        <beta>a*\</beta>
        <unic>&#x1FBA;</unic>
        <unid>&#x0391;&#x0300;</unid>
      </entry>
      <entry>
        <beta>a*|</beta>
        <unic>&#x1FBC;</unic>
        <unid>&#x0391;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a*_</beta>
        <unic>&#x1FB9;</unic>
        <unid>&#x0391;&#x0304;</unid>
      </entry>
      <entry>
        <beta>a*^</beta>
        <unic>&#x1FB8;</unic>
        <unid>&#x0391;&#x0306;</unid>
      </entry>
      <entry>
        <beta>a*</beta>
        <unic>&#x0391;</unic>
        <unid>&#x0391;</unid>
      </entry>
      <entry>
        <beta>a(/|</beta>
        <unic>&#x1F85;</unic>
        <unid>&#x03B1;&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a(/</beta>
        <unic>&#x1F05;</unic>
        <unid>&#x03B1;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>a(\|</beta>
        <unic>&#x1F83;</unic>
        <unid>&#x03B1;&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a(\</beta>
        <unic>&#x1F03;</unic>
        <unid>&#x03B1;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>a(=|</beta>
        <unic>&#x1F87;</unic>
        <unid>&#x03B1;&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a(=</beta>
        <unic>&#x1F07;</unic>
        <unid>&#x03B1;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>a(|</beta>
        <unic>&#x1F81;</unic>
        <unid>&#x03B1;&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a(</beta>
        <unic>&#x1F01;</unic>
        <unid>&#x03B1;&#x0314;</unid>
      </entry>
      <entry>
        <beta>a)/|</beta>
        <unic>&#x1F84;</unic>
        <unid>&#x03B1;&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a)/</beta>
        <unic>&#x1F04;</unic>
        <unid>&#x03B1;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>a)\|</beta>
        <unic>&#x1F82;</unic>
        <unid>&#x03B1;&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a)\</beta>
        <unic>&#x1F02;</unic>
        <unid>&#x03B1;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>a)=|</beta>
        <unic>&#x1F86;</unic>
        <unid>&#x03B1;&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a)=</beta>
        <unic>&#x1F06;</unic>
        <unid>&#x03B1;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>a)|</beta>
        <unic>&#x1F80;</unic>
        <unid>&#x03B1;&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a)</beta>
        <unic>&#x1F00;</unic>
        <unid>&#x03B1;&#x0313;</unid>
      </entry>
      <entry>
        <beta>a/|</beta>
        <unic>&#x1FB4;</unic>
        <unid>&#x03B1;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a/</beta>
        <unic>&#x03AC;</unic>
        <unid>&#x03B1;&#x0301;</unid>
      </entry>
      <entry>
        <beta>a\|</beta>
        <unic>&#x1FB2;</unic>
        <unid>&#x03B1;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a\</beta>
        <unic>&#x1F70;</unic>
        <unid>&#x03B1;&#x0300;</unid>
      </entry>
      <entry>
        <beta>a=|</beta>
        <unic>&#x1FB7;</unic>
        <unid>&#x03B1;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a=</beta>
        <unic>&#x1FB6;</unic>
        <unid>&#x03B1;&#x0342;</unid>
      </entry>
      <entry>
        <beta>a|</beta>
        <unic>&#x1FB3;</unic>
        <unid>&#x03B1;&#x0345;</unid>
      </entry>
      <entry>
        <beta>a_</beta>
        <unic>&#x1FB1;</unic>
        <unid>&#x03B1;&#x0304;</unid>
      </entry>
      <entry>
        <beta>a^</beta>
        <unic>&#x1FB0;</unic>
        <unid>&#x03B1;&#x0306;</unid>
      </entry>
      <entry>
        <beta>a</beta>
        <unic>&#x03B1;</unic>
        <unid>&#x03B1;</unid>
      </entry>
      <entry>
        <beta>b*</beta>
        <unic>&#x0392;</unic>
        <unid>&#x0392;</unid>
      </entry>
      <entry>
        <beta>b</beta>
        <unic>&#x03B2;</unic>
        <unid>&#x03B2;</unid>
      </entry>
      <entry>
        <beta>c*</beta>
        <unic>&#x039E;</unic>
        <unid>&#x039E;</unid>
      </entry>
      <entry>
        <beta>c</beta>
        <unic>&#x03BE;</unic>
        <unid>&#x03BE;</unid>
      </entry>
      <entry>
        <beta>d*</beta>
        <unic>&#x0394;</unic>
        <unid>&#x0394;</unid>
      </entry>
      <entry>
        <beta>d</beta>
        <unic>&#x03B4;</unic>
        <unid>&#x03B4;</unid>
      </entry>
      <entry>
        <beta>e*(/</beta>
        <unic>&#x1F1D;</unic>
        <unid>&#x0395;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>e*(\</beta>
        <unic>&#x1F1B;</unic>
        <unid>&#x0395;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>e*(</beta>
        <unic>&#x1F19;</unic>
        <unid>&#x0395;&#x0314;</unid>
      </entry>
      <entry>
        <beta>e*)/</beta>
        <unic>&#x1F1C;</unic>
        <unid>&#x0395;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>e*)\</beta>
        <unic>&#x1F1A;</unic>
        <unid>&#x0395;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>e*)</beta>
        <unic>&#x1F18;</unic>
        <unid>&#x0395;&#x0313;</unid>
      </entry>
      <entry>
        <beta>e*/</beta>
        <unic>&#x0388;</unic>
        <unid>&#x0395;&#x0301;</unid>
      </entry>
      <entry>
        <beta>e*\</beta>
        <unic>&#x1FC8;</unic>
        <unid>&#x0395;&#x0300;</unid>
      </entry>
      <entry>
        <beta>e*</beta>
        <unic>&#x0395;</unic>
        <unid>&#x0395;</unid>
      </entry>
      <entry>
        <beta>e(/</beta>
        <unic>&#x1F15;</unic>
        <unid>&#x03B5;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>e(\</beta>
        <unic>&#x1F13;</unic>
        <unid>&#x03B5;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>e(</beta>
        <unic>&#x1F11;</unic>
        <unid>&#x03B5;&#x0314;</unid>
      </entry>
      <entry>
        <beta>e)/</beta>
        <unic>&#x1F14;</unic>
        <unid>&#x03B5;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>e)\</beta>
        <unic>&#x1F12;</unic>
        <unid>&#x03B5;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>e)</beta>
        <unic>&#x1F10;</unic>
        <unid>&#x03B5;&#x0313;</unid>
      </entry>
      <entry>
        <beta>e/</beta>
        <unic>&#x03AD;</unic>
        <unid>&#x03B5;&#x0301;</unid>
      </entry>
      <entry>
        <beta>e\</beta>
        <unic>&#x1F72;</unic>
        <unid>&#x03B5;&#x0300;</unid>
      </entry>
      <entry>
        <beta>e</beta>
        <unic>&#x03B5;</unic>
        <unid>&#x03B5;</unid>
      </entry>
      <entry>
        <beta>f*</beta>
        <unic>&#x03A6;</unic>
        <unid>&#x03A6;</unid>
      </entry>
      <entry>
        <beta>f</beta>
        <unic>&#x03C6;</unic>
        <unid>&#x03C6;</unid>
      </entry>
      <entry>
        <beta>g*</beta>
        <unic>&#x0393;</unic>
        <unid>&#x0393;</unid>
      </entry>
      <entry>
        <beta>g</beta>
        <unic>&#x03B3;</unic>
        <unid>&#x03B3;</unid>
      </entry>
      <entry>
        <beta>h*(/|</beta>
        <unic>&#x1F9D;</unic>
        <unid>&#x0397;&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*(/</beta>
        <unic>&#x1F2D;</unic>
        <unid>&#x0397;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>h*(\|</beta>
        <unic>&#x1F9B;</unic>
        <unid>&#x0397;&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*(\</beta>
        <unic>&#x1F2B;</unic>
        <unid>&#x0397;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>h*(=|</beta>
        <unic>&#x1F9F;</unic>
        <unid>&#x0397;&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*(=</beta>
        <unic>&#x1F2F;</unic>
        <unid>&#x0397;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>h*(|</beta>
        <unic>&#x1F99;</unic>
        <unid>&#x0397;&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*(</beta>
        <unic>&#x1F29;</unic>
        <unid>&#x0397;&#x0314;</unid>
      </entry>
      <entry>
        <beta>h*)/|</beta>
        <unic>&#x1F9C;</unic>
        <unid>&#x0397;&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*)/</beta>
        <unic>&#x1F2C;</unic>
        <unid>&#x0397;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>h*)\|</beta>
        <unic>&#x1F9A;</unic>
        <unid>&#x0397;&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*)\</beta>
        <unic>&#x1F2A;</unic>
        <unid>&#x0397;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>h*)=|</beta>
        <unic>&#x1F9E;</unic>
        <unid>&#x0397;&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*)=</beta>
        <unic>&#x1F2E;</unic>
        <unid>&#x0397;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>h*)|</beta>
        <unic>&#x1F98;</unic>
        <unid>&#x0397;&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*)</beta>
        <unic>&#x1F28;</unic>
        <unid>&#x0397;&#x0313;</unid>
      </entry>
      <entry>
        <beta>h*/</beta>
        <unic>&#x0389;</unic>
        <unid>&#x0397;&#x0301;</unid>
      </entry>
      <entry>
        <beta>h*\</beta>
        <unic>&#x1FCA;</unic>
        <unid>&#x0397;&#x0300;</unid>
      </entry>
      <entry>
        <beta>h*|</beta>
        <unic>&#x1FCC;</unic>
        <unid>&#x0397;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h*</beta>
        <unic>&#x0397;</unic>
        <unid>&#x0397;</unid>
      </entry>
      <entry>
        <beta>h(/|</beta>
        <unic>&#x1F95;</unic>
        <unid>&#x03B7;&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h(/</beta>
        <unic>&#x1F25;</unic>
        <unid>&#x03B7;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>h(\|</beta>
        <unic>&#x1F93;</unic>
        <unid>&#x03B7;&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h(\</beta>
        <unic>&#x1F23;</unic>
        <unid>&#x03B7;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>h(=|</beta>
        <unic>&#x1F97;</unic>
        <unid>&#x03B7;&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h(=</beta>
        <unic>&#x1F27;</unic>
        <unid>&#x03B7;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>h(|</beta>
        <unic>&#x1F91;</unic>
        <unid>&#x03B7;&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h(</beta>
        <unic>&#x1F21;</unic>
        <unid>&#x03B7;&#x0314;</unid>
      </entry>
      <entry>
        <beta>h)/|</beta>
        <unic>&#x1F94;</unic>
        <unid>&#x03B7;&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h)/</beta>
        <unic>&#x1F24;</unic>
        <unid>&#x03B7;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>h)\|</beta>
        <unic>&#x1F92;</unic>
        <unid>&#x03B7;&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h)\</beta>
        <unic>&#x1F22;</unic>
        <unid>&#x03B7;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>h)=|</beta>
        <unic>&#x1F96;</unic>
        <unid>&#x03B7;&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h)=</beta>
        <unic>&#x1F26;</unic>
        <unid>&#x03B7;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>h)|</beta>
        <unic>&#x1F90;</unic>
        <unid>&#x03B7;&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h)</beta>
        <unic>&#x1F20;</unic>
        <unid>&#x03B7;&#x0313;</unid>
      </entry>
      <entry>
        <beta>h/|</beta>
        <unic>&#x1FC4;</unic>
        <unid>&#x03B7;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h/</beta>
        <unic>&#x03AE;</unic>
        <unid>&#x03B7;&#x0301;</unid>
      </entry>
      <entry>
        <beta>h\|</beta>
        <unic>&#x1FC2;</unic>
        <unid>&#x03B7;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h\</beta>
        <unic>&#x1F74;</unic>
        <unid>&#x03B7;&#x0300;</unid>
      </entry>
      <entry>
        <beta>h=|</beta>
        <unic>&#x1FC7;</unic>
        <unid>&#x03B7;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h=</beta>
        <unic>&#x1FC6;</unic>
        <unid>&#x03B7;&#x0342;</unid>
      </entry>
      <entry>
        <beta>h|</beta>
        <unic>&#x1FC3;</unic>
        <unid>&#x03B7;&#x0345;</unid>
      </entry>
      <entry>
        <beta>h</beta>
        <unic>&#x03B7;</unic>
        <unid>&#x03B7;</unid>
      </entry>
      <entry>
        <beta>i*(/</beta>
        <unic>&#x1F3D;</unic>
        <unid>&#x0399;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i*(\</beta>
        <unic>&#x1F3B;</unic>
        <unid>&#x0399;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i*(=</beta>
        <unic>&#x1F3F;</unic>
        <unid>&#x0399;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>i*(</beta>
        <unic>&#x1F39;</unic>
        <unid>&#x0399;&#x0314;</unid>
      </entry>
      <entry>
        <beta>i*)/</beta>
        <unic>&#x1F3C;</unic>
        <unid>&#x0399;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i*)\</beta>
        <unic>&#x1F3A;</unic>
        <unid>&#x0399;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i*)=</beta>
        <unic>&#x1F3E;</unic>
        <unid>&#x0399;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>i*)</beta>
        <unic>&#x1F38;</unic>
        <unid>&#x0399;&#x0313;</unid>
      </entry>
      <entry>
        <beta>i*+</beta>
        <unic>&#x03AA;</unic>
        <unid>&#x0399;&#x0308;</unid>
      </entry>
      <entry>
        <beta>i*/</beta>
        <unic>&#x038A;</unic>
        <unid>&#x0399;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i*\</beta>
        <unic>&#x1FDA;</unic>
        <unid>&#x0399;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i*_</beta>
        <unic>&#x1FD9;</unic>
        <unid>&#x0399;&#x0304;</unid>
      </entry>
      <entry>
        <beta>i*^</beta>
        <unic>&#x1FD8;</unic>
        <unid>&#x0399;&#x0306;</unid>
      </entry>
      <entry>
        <beta>i*</beta>
        <unic>&#x0399;</unic>
        <unid>&#x0399;</unid>
      </entry>
      <entry>
        <beta>i(/</beta>
        <unic>&#x1F35;</unic>
        <unid>&#x03B9;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i(\</beta>
        <unic>&#x1F33;</unic>
        <unid>&#x03B9;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i(=</beta>
        <unic>&#x1F37;</unic>
        <unid>&#x03B9;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>i(</beta>
        <unic>&#x1F31;</unic>
        <unid>&#x03B9;&#x0314;</unid>
      </entry>
      <entry>
        <beta>i)/</beta>
        <unic>&#x1F34;</unic>
        <unid>&#x03B9;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i)\</beta>
        <unic>&#x1F32;</unic>
        <unid>&#x03B9;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i)=</beta>
        <unic>&#x1F36;</unic>
        <unid>&#x03B9;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>i)</beta>
        <unic>&#x1F30;</unic>
        <unid>&#x03B9;&#x0313;</unid>
      </entry>
      <entry>
        <beta>i+/</beta>
        <unic>&#x0390;</unic>
        <unid>&#x03B9;&#x0308;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i+\</beta>
        <unic>&#x1FD2;</unic>
        <unid>&#x03B9;&#x0308;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i+=</beta>
        <unic>&#x1FD7;</unic>
        <unid>&#x03B9;&#x0308;&#x0342;</unid>
      </entry>
      <entry>
        <beta>i+</beta>
        <unic>&#x03CA;</unic>
        <unid>&#x03B9;&#x0308;</unid>
      </entry>
      <entry>
        <beta>i/</beta>
        <unic>&#x03AF;</unic>
        <unid>&#x03B9;&#x0301;</unid>
      </entry>
      <entry>
        <beta>i\</beta>
        <unic>&#x1F76;</unic>
        <unid>&#x03B9;&#x0300;</unid>
      </entry>
      <entry>
        <beta>i=</beta>
        <unic>&#x1FD6;</unic>
        <unid>&#x03B9;&#x0342;</unid>
      </entry>
      <entry>
        <beta>i_</beta>
        <unic>&#x1FD1;</unic>
        <unid>&#x03B9;&#x0304;</unid>
      </entry>
      <entry>
        <beta>i^</beta>
        <unic>&#x1FD0;</unic>
        <unid>&#x03B9;&#x0306;</unid>
      </entry>
      <entry>
        <beta>i</beta>
        <unic>&#x03B9;</unic>
        <unid>&#x03B9;</unid>
      </entry>
      <entry>
        <beta>k*</beta>
        <unic>&#x039A;</unic>
        <unid>&#x039A;</unid>
      </entry>
      <entry>
        <beta>k</beta>
        <unic>&#x03BA;</unic>
        <unid>&#x03BA;</unid>
      </entry>
      <entry>
        <beta>l*</beta>
        <unic>&#x039B;</unic>
        <unid>&#x039B;</unid>
      </entry>
      <entry>
        <beta>l</beta>
        <unic>&#x03BB;</unic>
        <unid>&#x03BB;</unid>
      </entry>
      <entry>
        <beta>m*</beta>
        <unic>&#x039C;</unic>
        <unid>&#x039C;</unid>
      </entry>
      <entry>
        <beta>m</beta>
        <unic>&#x03BC;</unic>
        <unid>&#x03BC;</unid>
      </entry>
      <entry>
        <beta>n*</beta>
        <unic>&#x039D;</unic>
        <unid>&#x039D;</unid>
      </entry>
      <entry>
        <beta>n</beta>
        <unic>&#x03BD;</unic>
        <unid>&#x03BD;</unid>
      </entry>
      <entry>
        <beta>o*(/</beta>
        <unic>&#x1F4D;</unic>
        <unid>&#x039F;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>o*(\</beta>
        <unic>&#x1F4B;</unic>
        <unid>&#x039F;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>o*(</beta>
        <unic>&#x1F49;</unic>
        <unid>&#x039F;&#x0314;</unid>
      </entry>
      <entry>
        <beta>o*)/</beta>
        <unic>&#x1F4C;</unic>
        <unid>&#x039F;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>o*)\</beta>
        <unic>&#x1F4A;</unic>
        <unid>&#x039F;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>o*)</beta>
        <unic>&#x1F48;</unic>
        <unid>&#x039F;&#x0313;</unid>
      </entry>
      <entry>
        <beta>o*/</beta>
        <unic>&#x038C;</unic>
        <unid>&#x039F;&#x0301;</unid>
      </entry>
      <entry>
        <beta>o*\</beta>
        <unic>&#x1FF8;</unic>
        <unid>&#x039F;&#x0300;</unid>
      </entry>
      <entry>
        <beta>o*</beta>
        <unic>&#x039F;</unic>
        <unid>&#x039F;</unid>
      </entry>
      <entry>
        <beta>o(/</beta>
        <unic>&#x1F45;</unic>
        <unid>&#x03BF;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>o(\</beta>
        <unic>&#x1F43;</unic>
        <unid>&#x03BF;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>o(</beta>
        <unic>&#x1F41;</unic>
        <unid>&#x03BF;&#x0314;</unid>
      </entry>
      <entry>
        <beta>o)/</beta>
        <unic>&#x1F44;</unic>
        <unid>&#x03BF;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>o)\</beta>
        <unic>&#x1F42;</unic>
        <unid>&#x03BF;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>o)</beta>
        <unic>&#x1F40;</unic>
        <unid>&#x03BF;&#x0313;</unid>
      </entry>
      <entry>
        <beta>o/</beta>
        <unic>&#x03CC;</unic>
        <unid>&#x03BF;&#x0301;</unid>
      </entry>
      <entry>
        <beta>o\</beta>
        <unic>&#x1F78;</unic>
        <unid>&#x03BF;&#x0300;</unid>
      </entry>
      <entry>
        <beta>o</beta>
        <unic>&#x03BF;</unic>
        <unid>&#x03BF;</unid>
      </entry>
      <entry>
        <beta>p*</beta>
        <unic>&#x03A0;</unic>
        <unid>&#x03A0;</unid>
      </entry>
      <entry>
        <beta>p</beta>
        <unic>&#x03C0;</unic>
        <unid>&#x03C0;</unid>
      </entry>
      <entry>
        <beta>q*</beta>
        <unic>&#x0398;</unic>
        <unid>&#x0398;</unid>
      </entry>
      <entry>
        <beta>q</beta>
        <unic>&#x03B8;</unic>
        <unid>&#x03B8;</unid>
      </entry>
      <entry>
        <beta>r*(</beta>
        <unic>&#x1FEC;</unic>
        <unid>&#x03A1;&#x0314;</unid>
      </entry>
      <entry>
        <beta>r*</beta>
        <unic>&#x03A1;</unic>
        <unid>&#x03A1;</unid>
      </entry>
      <entry>
        <beta>r(</beta>
        <unic>&#x1FE5;</unic>
        <unid>&#x03C1;&#x0314;</unid>
      </entry>
      <entry>
        <beta>r)</beta>
        <unic>&#x1FE4;</unic>
        <unid>&#x03C1;&#x0313;</unid>
      </entry>
      <entry>
        <beta>r</beta>
        <unic>&#x03C1;</unic>
        <unid>&#x03C1;</unid>
      </entry>
      <entry>
        <beta>s*</beta>
        <unic>&#x03A3;</unic>
        <unid>&#x03A3;</unid>
      </entry>
      <entry>
        <beta>s</beta>
        <unic>&#x03C3;</unic>
        <unid>&#x03C3;</unid>
      </entry>
      <entry>
        <beta>t*</beta>
        <unic>&#x03A4;</unic>
        <unid>&#x03A4;</unid>
      </entry>
      <entry>
        <beta>t</beta>
        <unic>&#x03C4;</unic>
        <unid>&#x03C4;</unid>
      </entry>
      <entry>
        <beta>u*(/</beta>
        <unic>&#x1F5D;</unic>
        <unid>&#x03A5;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>u*(\</beta>
        <unic>&#x1F5B;</unic>
        <unid>&#x03A5;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>u*(=</beta>
        <unic>&#x1F5F;</unic>
        <unid>&#x03A5;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>u*(</beta>
        <unic>&#x1F59;</unic>
        <unid>&#x03A5;&#x0314;</unid>
      </entry>
      <entry>
        <beta>u*+</beta>
        <unic>&#x03AB;</unic>
        <unid>&#x03A5;&#x0308;</unid>
      </entry>
      <entry>
        <beta>u*/</beta>
        <unic>&#x038E;</unic>
        <unid>&#x03A5;&#x0301;</unid>
      </entry>
      <entry>
        <beta>u*\</beta>
        <unic>&#x1FEA;</unic>
        <unid>&#x03A5;&#x0300;</unid>
      </entry>
      <entry>
        <beta>u*_</beta>
        <unic>&#x1FE9;</unic>
        <unid>&#x03A5;&#x0304;</unid>
      </entry>
      <entry>
        <beta>u*^</beta>
        <unic>&#x1FE8;</unic>
        <unid>&#x03A5;&#x0306;</unid>
      </entry>
      <entry>
        <beta>u*</beta>
        <unic>&#x03A5;</unic>
        <unid>&#x03A5;</unid>
      </entry>
      <entry>
        <beta>u(/</beta>
        <unic>&#x1F55;</unic>
        <unid>&#x03C5;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>u(\</beta>
        <unic>&#x1F53;</unic>
        <unid>&#x03C5;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>u(=</beta>
        <unic>&#x1F57;</unic>
        <unid>&#x03C5;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>u(</beta>
        <unic>&#x1F51;</unic>
        <unid>&#x03C5;&#x0314;</unid>
      </entry>
      <entry>
        <beta>u)/</beta>
        <unic>&#x1F54;</unic>
        <unid>&#x03C5;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>u)\</beta>
        <unic>&#x1F52;</unic>
        <unid>&#x03C5;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>u)=</beta>
        <unic>&#x1F56;</unic>
        <unid>&#x03C5;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>u)</beta>
        <unic>&#x1F50;</unic>
        <unid>&#x03C5;&#x0313;</unid>
      </entry>
      <entry>
        <beta>u+/</beta>
        <unic>&#x03B0;</unic>
        <unid>&#x03C5;&#x0308;&#x0301;</unid>
      </entry>
      <entry>
        <beta>u+\</beta>
        <unic>&#x1FE2;</unic>
        <unid>&#x03C5;&#x0308;&#x0300;</unid>
      </entry>
      <entry>
        <beta>u+=</beta>
        <unic>&#x1FE7;</unic>
        <unid>&#x03C5;&#x0308;&#x0342;</unid>
      </entry>
      <entry>
        <beta>u+</beta>
        <unic>&#x03CB;</unic>
        <unid>&#x03C5;&#x0308;</unid>
      </entry>
      <entry>
        <beta>u/</beta>
        <unic>&#x03CD;</unic>
        <unid>&#x03C5;&#x0301;</unid>
      </entry>
      <entry>
        <beta>u\</beta>
        <unic>&#x1F7A;</unic>
        <unid>&#x03C5;&#x0300;</unid>
      </entry>
      <entry>
        <beta>u=</beta>
        <unic>&#x1FE6;</unic>
        <unid>&#x03C5;&#x0342;</unid>
      </entry>
      <entry>
        <beta>u_</beta>
        <unic>&#x1FE1;</unic>
        <unid>&#x03C5;&#x0304;</unid>
      </entry>
      <entry>
        <beta>u^</beta>
        <unic>&#x1FE0;</unic>
        <unid>&#x03C5;&#x0306;</unid>
      </entry>
      <entry>
        <beta>u</beta>
        <unic>&#x03C5;</unic>
        <unid>&#x03C5;</unid>
      </entry>
      <entry>
        <beta>v*</beta>
        <unic>&#x03DC;</unic>
        <unid>&#x03DC;</unid>
      </entry>
      <entry>
        <beta>v</beta>
        <unic>&#x03DD;</unic>
        <unid>&#x03DD;</unid>
      </entry>
      <entry>
        <beta>w*(/|</beta>
        <unic>&#x1FAD;</unic>
        <unid>&#x03A9;&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*(/</beta>
        <unic>&#x1F6D;</unic>
        <unid>&#x03A9;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>w*(\|</beta>
        <unic>&#x1FAB;</unic>
        <unid>&#x03A9;&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*(\</beta>
        <unic>&#x1F6B;</unic>
        <unid>&#x03A9;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>w*(=|</beta>
        <unic>&#x1FAF;</unic>
        <unid>&#x03A9;&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*(=</beta>
        <unic>&#x1F6F;</unic>
        <unid>&#x03A9;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>w*(|</beta>
        <unic>&#x1FA9;</unic>
        <unid>&#x03A9;&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*(</beta>
        <unic>&#x1F69;</unic>
        <unid>&#x03A9;&#x0314;</unid>
      </entry>
      <entry>
        <beta>w*)/|</beta>
        <unic>&#x1FAC;</unic>
        <unid>&#x03A9;&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*)/</beta>
        <unic>&#x1F6C;</unic>
        <unid>&#x03A9;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>w*)\|</beta>
        <unic>&#x1FAA;</unic>
        <unid>&#x03A9;&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*)\</beta>
        <unic>&#x1F6A;</unic>
        <unid>&#x03A9;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>w*)=|</beta>
        <unic>&#x1FAE;</unic>
        <unid>&#x03A9;&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*)=</beta>
        <unic>&#x1F6E;</unic>
        <unid>&#x03A9;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>w*)|</beta>
        <unic>&#x1FA8;</unic>
        <unid>&#x03A9;&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*)</beta>
        <unic>&#x1F68;</unic>
        <unid>&#x03A9;&#x0313;</unid>
      </entry>
      <entry>
        <beta>w*/</beta>
        <unic>&#x038F;</unic>
        <unid>&#x03A9;&#x0301;</unid>
      </entry>
      <entry>
        <beta>w*\</beta>
        <unic>&#x1FFA;</unic>
        <unid>&#x03A9;&#x0300;</unid>
      </entry>
      <entry>
        <beta>w*|</beta>
        <unic>&#x1FFC;</unic>
        <unid>&#x03A9;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w*</beta>
        <unic>&#x03A9;</unic>
        <unid>&#x03A9;</unid>
      </entry>
      <entry>
        <beta>w(/|</beta>
        <unic>&#x1FA5;</unic>
        <unid>&#x03C9;&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w(/</beta>
        <unic>&#x1F65;</unic>
        <unid>&#x03C9;&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>w(\|</beta>
        <unic>&#x1FA3;</unic>
        <unid>&#x03C9;&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w(\</beta>
        <unic>&#x1F63;</unic>
        <unid>&#x03C9;&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>w(=|</beta>
        <unic>&#x1FA7;</unic>
        <unid>&#x03C9;&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w(=</beta>
        <unic>&#x1F67;</unic>
        <unid>&#x03C9;&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>w(|</beta>
        <unic>&#x1FA1;</unic>
        <unid>&#x03C9;&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w(</beta>
        <unic>&#x1F61;</unic>
        <unid>&#x03C9;&#x0314;</unid>
      </entry>
      <entry>
        <beta>w)/|</beta>
        <unic>&#x1FA4;</unic>
        <unid>&#x03C9;&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w)/</beta>
        <unic>&#x1F64;</unic>
        <unid>&#x03C9;&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>w)\|</beta>
        <unic>&#x1FA2;</unic>
        <unid>&#x03C9;&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w)\</beta>
        <unic>&#x1F62;</unic>
        <unid>&#x03C9;&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>w)=|</beta>
        <unic>&#x1FA6;</unic>
        <unid>&#x03C9;&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w)=</beta>
        <unic>&#x1F66;</unic>
        <unid>&#x03C9;&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>w)|</beta>
        <unic>&#x1FA0;</unic>
        <unid>&#x03C9;&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w)</beta>
        <unic>&#x1F60;</unic>
        <unid>&#x03C9;&#x0313;</unid>
      </entry>
      <entry>
        <beta>w/|</beta>
        <unic>&#x1FF4;</unic>
        <unid>&#x03C9;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w/</beta>
        <unic>&#x03CE;</unic>
        <unid>&#x03C9;&#x0301;</unid>
      </entry>
      <entry>
        <beta>w\|</beta>
        <unic>&#x1FF2;</unic>
        <unid>&#x03C9;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w\</beta>
        <unic>&#x1F7C;</unic>
        <unid>&#x03C9;&#x0300;</unid>
      </entry>
      <entry>
        <beta>w=|</beta>
        <unic>&#x1FF7;</unic>
        <unid>&#x03C9;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w=</beta>
        <unic>&#x1FF6;</unic>
        <unid>&#x03C9;&#x0342;</unid>
      </entry>
      <entry>
        <beta>w|</beta>
        <unic>&#x1FF3;</unic>
        <unid>&#x03C9;&#x0345;</unid>
      </entry>
      <entry>
        <beta>w</beta>
        <unic>&#x03C9;</unic>
        <unid>&#x03C9;</unid>
      </entry>
      <entry>
        <beta>x*</beta>
        <unic>&#x03A7;</unic>
        <unid>&#x03A7;</unid>
      </entry>
      <entry>
        <beta>x</beta>
        <unic>&#x03C7;</unic>
        <unid>&#x03C7;</unid>
      </entry>
      <entry>
        <beta>y*</beta>
        <unic>&#x03A8;</unic>
        <unid>&#x03A8;</unid>
      </entry>
      <entry>
        <beta>y</beta>
        <unic>&#x03C8;</unic>
        <unid>&#x03C8;</unid>
      </entry>
      <entry>
        <beta>z*</beta>
        <unic>&#x0396;</unic>
        <unid>&#x0396;</unid>
      </entry>
      <entry>
        <beta>z</beta>
        <unic>&#x03B6;</unic>
        <unid>&#x03B6;</unid>
      </entry>
      <!--
          entries for diacritics only
          here <unic> holds non-combining form,
          <unid> holds combining form 
      -->
      <entry>
        <beta>(/|</beta>
        <unic>&#x1FDE;&#x0345;</unic>
        <unid>&#x0314;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>(/</beta>
        <unic>&#x1FDE;</unic>
        <unid>&#x0314;&#x0301;</unid>
      </entry>
      <entry>
        <beta>(\|</beta>
        <unic>&#x1FDD;&#x0345;</unic>
        <unid>&#x0314;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>(\</beta>
        <unic>&#x1FDD;</unic>
        <unid>&#x0314;&#x0300;</unid>
      </entry>
      <entry>
        <beta>(=|</beta>
        <unic>&#x1FDF;&#x0345;</unic>
        <unid>&#x0314;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>(=</beta>
        <unic>&#x1FDF;</unic>
        <unid>&#x0314;&#x0342;</unid>
      </entry>
      <entry>
        <beta>(|</beta>
        <unic>&#x02BD;&#x0345;</unic>
        <unid>&#x0314;&#x0345;</unid>
      </entry>
      <entry>
        <beta>(</beta>
        <unic>&#x02BD;</unic>
        <unid>&#x0314;</unid>
      </entry>
      <entry>
        <beta>)/|</beta>
        <unic>&#x1FCE;&#x0345;</unic>
        <unid>&#x0313;&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>)/</beta>
        <unic>&#x1FCE;</unic>
        <unid>&#x0313;&#x0301;</unid>
      </entry>
      <entry>
        <beta>)\|</beta>
        <unic>&#x1FCD;&#x0345;</unic>
        <unid>&#x0313;&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>)\</beta>
        <unic>&#x1FCD;</unic>
        <unid>&#x0313;&#x0300;</unid>
      </entry>
      <entry>
        <beta>)=|</beta>
        <unic>&#x1FCF;&#x0345;</unic>
        <unid>&#x0313;&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>)=</beta>
        <unic>&#x1FCF;</unic>
        <unid>&#x0313;&#x0342;</unid>
      </entry>
      <entry>
        <beta>)|</beta>
        <unic>&#x02BC;&#x0345;</unic>
        <unid>&#x0313;&#x0345;</unid>
      </entry>
      <entry>
        <beta>)</beta>
        <unic>&#x02BC;</unic>
        <unid>&#x0313;</unid>
      </entry>
      <entry>
        <beta>+/</beta>
        <unic>&#x1FEE;</unic>
        <unid>&#x0308;&#x0301;</unid>
      </entry>
      <entry>
        <beta>+\</beta>
        <unic>&#x1FED;</unic>
        <unid>&#x0308;&#x0300;</unid>
      </entry>
      <entry>
        <beta>+=</beta>
        <unic>&#x1FC1;</unic>
        <unid>&#x0308;&#x0342;</unid>
      </entry>
      <entry>
        <beta>+</beta>
        <unic>&#x00A8;</unic>
        <unid>&#x0308;</unid>
      </entry>
      <entry>
        <beta>/|</beta>
        <unic>&#x00B4;&#x0345;</unic>
        <unid>&#x0301;&#x0345;</unid>
      </entry>
      <entry>
        <beta>/</beta>
        <unic>&#x00B4;</unic>
        <unid>&#x0301;</unid>
      </entry>
      <entry>
        <beta>\|</beta>
        <unic>&#x0060;&#x0345;</unic>
        <unid>&#x0300;&#x0345;</unid>
      </entry>
      <entry>
        <beta>\</beta>
        <unic>&#x0060;</unic>
        <unid>&#x0300;</unid>
      </entry>
      <entry>
        <beta>=|</beta>
        <unic>&#x1FC0;&#x0345;</unic>
        <unid>&#x0342;&#x0345;</unid>
      </entry>
      <entry>
        <beta>=</beta>
        <unic>&#x1FC0;</unic>
        <unid>&#x0342;</unid>
      </entry>
      <entry>
        <beta>|</beta>
        <unic>&#x1FBE;</unic>
        <unid>&#x0345;</unid>
      </entry>
      <entry>
        <beta>_</beta>
        <unic>&#x00AF;</unic>
        <unid>&#x0304;</unid>
      </entry>
      <entry>
        <beta>^</beta>
        <unic>&#x02D8;</unic>
        <unid>&#x0306;</unid>
      </entry>
      <entry>
        <beta>&apos;</beta>
        <unic>&#x1FBD;</unic>
        <unid>&#x1FBD;</unid>
      </entry>
      <!--
        entries for character forms in the extended Greek page
        that also appear in the regular Greek page
        These ensure that alternate encodings are properly
        translated to betacode, since only the preferred
        encoding from the regular page is listed above.
        These should appear last since the beta-to-unicode
        encoding uses the first entry found.
      -->
      <entry>
        <beta>a*/</beta>
        <unic>&#x1FBB;</unic>
        <unid>&#x1FBB;</unid>
      </entry>
      <entry>
        <beta>a/</beta>
        <unic>&#x1F71;</unic>
        <unid>&#x1F71;</unid>
      </entry>
      <entry>
        <beta>e*/</beta>
        <unic>&#x1FC9;</unic>
        <unid>&#x1FC9;</unid>
      </entry>
      <entry>
        <beta>e/</beta>
        <unic>&#x1F73;</unic>
        <unid>&#x1F73;</unid>
      </entry>
      <entry>
        <beta>h*/</beta>
        <unic>&#x1FCB;</unic>
        <unid>&#x1FCB;</unid>
      </entry>
      <entry>
        <beta>h/</beta>
        <unic>&#x1F75;</unic>
        <unid>&#x1F75;</unid>
      </entry>
      <entry>
        <beta>i*/</beta>
        <unic>&#x1FDB;</unic>
        <unid>&#x1FDB;</unid>
      </entry>
      <entry>
        <beta>i/</beta>
        <unic>&#x1F77;</unic>
        <unid>&#x1F77;</unid>
      </entry>
      <entry>
        <beta>i+/</beta>
        <unic>&#x1FD3;</unic>
        <unid>&#x1FD3;</unid>
      </entry>
      <entry>
        <beta>o*/</beta>
        <unic>&#x1FF9;</unic>
        <unid>&#x1FF9;</unid>
      </entry>
      <entry>
        <beta>o/</beta>
        <unic>&#x1F79;</unic>
        <unid>&#x1F79;</unid>
      </entry>
      <entry>
        <beta>s</beta>
        <unic>&#x03C2;</unic>
        <unid>&#x03C2;</unid>
      </entry>
      <entry>
        <beta>u*/</beta>
        <unic>&#x1FEB;</unic>
        <unid>&#x1FEB;</unid>
      </entry>
      <entry>
        <beta>u/</beta>
        <unic>&#x1F7B;</unic>
        <unid>&#x1F7B;</unid>
      </entry>
      <entry>
        <beta>u+/</beta>
        <unic>&#x1FE3;</unic>
        <unid>&#x1FE3;</unid>
      </entry>
      <entry>
        <beta>w*/</beta>
        <unic>&#x1FFB;</unic>
        <unid>&#x1FFB;</unid>
      </entry>
      <entry>
        <beta>w/</beta>
        <unic>&#x1F7D;</unic>
        <unid>&#x1F7D;</unid>
      </entry>
      <!--
        entries for special betacodes
      -->
      <!-- medial sigma -->
      <entry>
        <beta>s1</beta>
        <unic>&#x03C3;</unic>
        <unid>&#x03C3;</unid>
      </entry>
      <!-- final sigma -->
      <entry>
        <beta>s2</beta>
        <unic>&#x03C2;</unic>
        <unid>&#x03C2;</unid>
      </entry>
    </beta-uni-table>
  </xsl:variable>
  <xsl:variable name="s_betaUniTable"
    select="exsl:node-set($s_rawTable)/beta-uni-table"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*"></xsl:apply-templates>
      <xsl:apply-templates select="node()"></xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@span|@lemma|@form">
    <xsl:choose>
      <xsl:when test="ancestor::treebank[@xml:lang='grc' or @xml:lang='greek' or @lang='grc' or @lang='greek']">
        <xsl:attribute name="{local-name(.)}">
          <xsl:call-template name="beta-to-uni">
            <xsl:with-param name="a_in" select="."/>                    
          </xsl:call-template>              
        </xsl:attribute>    
      </xsl:when>
      <xsl:otherwise><xsl:copy/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Insert betacode diacritic character in sorted order in string
    Parameters:
      $a_string       existing string
      $a_char         character to be inserted

    Output:
      updated string with character inserted in canonical order
  -->
  <xsl:template name="insert-diacritic">
    <xsl:param name="a_string"/>
    <xsl:param name="a_char"/>

    <xsl:choose>
      <!-- if empty string, use char -->
      <xsl:when test="string-length($a_string) = 0">
        <xsl:value-of select="$a_char"/>
      </xsl:when>

      <xsl:otherwise>
        <!-- find order of char and head of string -->
        <xsl:variable name="head" select="substring($a_string, 1, 1)"/>
        <xsl:variable name="charOrder">
          <xsl:call-template name="beta-order">
            <xsl:with-param name="a_beta" select="$a_char"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="headOrder">
          <xsl:call-template name="beta-order">
            <xsl:with-param name="a_beta" select="$head"/>
          </xsl:call-template>
        </xsl:variable>

        <xsl:choose>
          <!-- if new char is greater than head, insert it in remainder -->
          <xsl:when test="number($charOrder) > number($headOrder)">
            <xsl:variable name="tail">
              <xsl:call-template name="insert-diacritic">
                <xsl:with-param name="a_string" select="substring($a_string, 2)"/>
                <xsl:with-param name="a_char" select="$a_char"/>
              </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="concat($head, $tail)"/>
          </xsl:when>

          <!-- if same as head, discard it (don't want duplicates) -->
          <xsl:when test="number($charOrder) = number($headOrder)">
            <xsl:value-of select="$a_string"/>
          </xsl:when>

          <!-- if new char comes before head -->
          <xsl:otherwise>
            <xsl:value-of select="concat($a_char, $a_string)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Define canonical order of betacode diacritics
    Parameter:
      $a_beta        betacode diacritic character

    Output:
      numerical order of character in canonical ordering
  -->
  <xsl:template name="beta-order">
    <xsl:param name="a_beta"/>
    <xsl:choose>
      <!-- capitalization -->
      <xsl:when test="$a_beta = '*'">0</xsl:when>
      <!-- dasia -->
      <xsl:when test="$a_beta = '('">1</xsl:when>
      <!-- psili -->
      <xsl:when test="$a_beta = ')'">2</xsl:when>
      <!-- diaeresis -->
      <xsl:when test="$a_beta = '+'">3</xsl:when>
      <!-- acute -->
      <xsl:when test="$a_beta = '/'">4</xsl:when>
      <!-- grave -->
      <xsl:when test="$a_beta = '\'">5</xsl:when>
      <!-- perispomeni -->
      <xsl:when test="$a_beta = '='">6</xsl:when>
      <!-- ypogegrammeni -->
      <xsl:when test="$a_beta = '|'">7</xsl:when>
      <!-- macron -->
      <xsl:when test="$a_beta = '_'">8</xsl:when>
      <!-- breve -->
      <xsl:when test="$a_beta = '^'">9</xsl:when>
      <!-- koronis -->
      <xsl:when test="$a_beta = &quot;&apos;&quot;">10</xsl:when>
    </xsl:choose>
  </xsl:template>

  <!--
    Strip vowel length diacritics and/or capitalization from betacode
    Parameters:
      $a_in               	string to strip
      $a_stripVowels     	whether to strip vowel length diacritics
      $a_stripDiaereses  	whether to strip diaeresis diacritics
      $a_stripCaps       	whether to strip capitalization
      $a_stripString     	betacode characters to remove
  -->
  <xsl:template name="beta-strip">
    <xsl:param name="a_in"/>
    <xsl:param name="a_stripVowels" select="true()"/>
    <xsl:param name="a_stripDiaereses" select="true()"/>
    <xsl:param name="a_stripCaps" select="true()"/>
    <xsl:param name="a_stripString" select="''"/>

    <!-- strip vowels if requested -->
    <xsl:variable name="temp1">
      <xsl:choose>
        <xsl:when test="$a_stripVowels">
          <xsl:value-of
            select="translate($a_in,
                              $s_betaWithLength,
                              $s_betaWithoutLength)"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$a_in"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- strip diaereses if requested -->
    <xsl:variable name="temp2">
      <xsl:choose>
        <xsl:when test="$a_stripDiaereses">
          <xsl:value-of
            select="translate($temp1,
                              $s_betaWithDiaeresis,
                              $s_betaWithoutDiaeresis)"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$temp1"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- strip capitalization if requested -->
    <xsl:variable name="temp3">
      <xsl:choose>
        <xsl:when test="$a_stripCaps">
          <xsl:value-of
            select="translate($temp2,
                              $s_betaWithCaps,
                              $s_betaWithoutCaps)"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$temp2"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- strip characters if requested -->
    <xsl:choose>
      <xsl:when test="string-length($a_stripString) > 0">
        <xsl:value-of select="translate($temp3, $a_stripString, '')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$temp3"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!--
    Strip vowel length diacritics and/or capitalization from unicode
    Parameters:
    $a_in	          	string to strip
    $a_stripVowels     	whether to strip vowel length diacritics
    $a_stripDiaereses  	whether to strip diaeresis diacritics
    $a_stripCaps       	whether to strip capitalization
  -->
  <xsl:template name="uni-strip">
    <xsl:param name="a_in"/>
    <xsl:param name="a_stripVowels" select="true()"/>
    <xsl:param name="a_stripDiaereses" select="true()"/>
    <xsl:param name="a_stripCaps" select="true()"/>

    <!-- strip vowels if requested -->
    <xsl:variable name="temp1">
      <xsl:choose>
        <xsl:when test="$a_stripVowels">
          <xsl:value-of
            select="translate($a_in,
                              $s_uniWithLength,
                              $s_uniWithoutLength)"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$a_in"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- strip diaereses if requested -->
    <xsl:variable name="temp2">
      <xsl:choose>
        <xsl:when test="$a_stripDiaereses">
          <xsl:value-of
            select="translate($temp1,
                              $s_uniWithDiaeresis,
                              $s_uniWithoutDiaeresis)"
          />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$temp1"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- strip capitalization if requested -->
    <xsl:choose>
      <xsl:when test="$a_stripCaps">
        <xsl:value-of
          select="translate($temp2, $s_uniWithCaps, $s_uniWithoutCaps)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$temp2"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Convert betacode to unicode
    Parameters:
      $a_key          combined character plus diacritics
      $a_precomposed  whether to put out precomposed or decomposed Unicode
  -->
  <xsl:template match="beta-uni-table" mode="b2u">
    <xsl:param name="a_key"/>
    <xsl:param name="a_precomposed"/>

    <xsl:variable name="keylen" select="string-length($a_key)"/>

    <!-- if key exists -->
    <xsl:if test="$keylen > 0">
      <!-- try to find key in table -->
      <xsl:variable name="value">
        <xsl:choose>
          <xsl:when test="$a_precomposed">
            <xsl:value-of select="(key('s_betaUniLookup', $a_key)/unic)[1]/text()"
            />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="(key('s_betaUniLookup', $a_key)/unid)[1]/text()"
            />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:choose>
        <!-- if key found, use value -->
        <xsl:when test="string-length($value) > 0">
          <xsl:value-of select="$value"/>
        </xsl:when>

        <!-- if key not found and contains multiple chars -->
        <xsl:when test="$keylen > 1">
          <!-- lookup key with last char removed -->
          <xsl:apply-templates select="$s_betaUniTable" mode="b2u">
            <xsl:with-param name="a_key" select="substring($a_key, 1, $keylen - 1)"/>
            <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
          </xsl:apply-templates>
          <!-- convert last char -->
          <!-- a_precomposed=false means make sure it's a combining form -->
          <xsl:apply-templates select="$s_betaUniTable" mode="b2u">
            <xsl:with-param name="a_key" select="substring($a_key, $keylen)"/>
            <xsl:with-param name="a_precomposed" select="false()"/>
          </xsl:apply-templates>
        </xsl:when>
      </xsl:choose>

      <!-- otherwise, ignore it (probably an errant *) -->
    </xsl:if>
  </xsl:template>

  <!--
    Convert unicode to betacode
    Parameters:
      $a_key          Unicode character to look up
  -->
  <xsl:template match="beta-uni-table" mode="u2b">
    <xsl:param name="a_key"/>
    <xsl:value-of select="(key('s_unicBetaLookup', $a_key)/beta)[1]/text()"/>
  </xsl:template>

<!--
    Convert Unicode to Greek betacode
    Parameters:
      $a_in           Unicode input string to be converted
      $a_pending      betacode character waiting to be output
      $a_state        betacode diacritics associated with pending character
      $a_upper        Whether to output base characters in upper or lower case

    Output:
      $a_in transformed to equivalent betacode

    Betacode diacritics for a capital letter precede the base letter.
    Therefore, we must look ahead to find any trailing combining diacritics
    in the Unicode before we can properly output a capital letter.
  -->
  <xsl:template name="uni-to-beta">
    <xsl:param name="a_in"/>
    <xsl:param name="a_pending" select="''"/>
    <xsl:param name="a_state" select="''"/>
    <xsl:param name="a_upper" select="true()"/>

    <xsl:variable name="head" select="substring($a_in, 1, 1)"/>

    <xsl:choose>
      <!-- if no more input -->
      <xsl:when test="string-length($a_in) = 0">
        <!-- output last pending char -->
        <xsl:call-template name="output-beta-char">
          <xsl:with-param name="a_char" select="$a_pending"/>
          <xsl:with-param name="a_state" select="$a_state"/>
        </xsl:call-template>
      </xsl:when>

      <!-- if input starts with diacritic -->
      <xsl:when test="contains($s_uniDiacritics, $head) and ($head != ' ')">
        <!-- recurse with diacritic added to state -->
        <xsl:call-template name="uni-to-beta">
          <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
          <xsl:with-param name="a_state">
            <xsl:call-template name="insert-diacritic">
              <xsl:with-param name="a_string" select="$a_state"/>
              <xsl:with-param name="a_char"
                select="translate($head, $s_uniDiacritics, $s_betaDiacritics)"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="a_pending" select="$a_pending"/>
          <xsl:with-param name="a_upper" select="$a_upper"/>
        </xsl:call-template>
      </xsl:when>

      <!-- if not a special char -->
      <xsl:otherwise>
        <!-- output pending char -->
        <xsl:call-template name="output-beta-char">
          <xsl:with-param name="a_char" select="$a_pending"/>
          <xsl:with-param name="a_state" select="$a_state"/>
        </xsl:call-template>

        <!-- look up unicode in table -->
        <xsl:variable name="beta">
          <xsl:apply-templates select="$s_betaUniTable" mode="u2b">
            <xsl:with-param name="a_key" select="$head"/>
          </xsl:apply-templates>
        </xsl:variable>

        <xsl:choose>
          <!-- if we found anything in lookup, use it -->
          <!-- Strings in lookup table are lowercase base character -->
          <!-- plus optional asterisk plus optional diacritics -->
          <xsl:when test="string-length($beta) > 0">
            <xsl:variable name="base" select="substring($beta, 1, 1)"/>

            <!-- recurse with base, in requested case, as pending character -->
            <xsl:call-template name="uni-to-beta">
              <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
              <xsl:with-param name="a_state" select="substring($beta, 2)"/>
              <xsl:with-param name="a_pending">
                <xsl:choose>
                  <xsl:when test="$a_upper">
                    <xsl:value-of
                      select="translate($base, $s_betaLowers, $s_betaUppers)"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$base"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:with-param>
              <xsl:with-param name="a_upper" select="$a_upper"/>
            </xsl:call-template>
          </xsl:when>

          <!-- otherwise, recurse with next character as pending -->
          <xsl:otherwise>
            <xsl:call-template name="uni-to-beta">
              <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
              <xsl:with-param name="a_state" select="''"/>
              <xsl:with-param name="a_pending" select="$head"/>
              <xsl:with-param name="a_upper" select="$a_upper"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Output a single character with diacritics
    Parameters:
      $a_char         character to be output
      $a_state        diacritics associated with character
  -->
  <xsl:template name="output-beta-char">
    <xsl:param name="a_char"/>
    <xsl:param name="a_state"/>

    <xsl:choose>
      <!-- if capital letter -->
      <xsl:when test="substring($a_state, 1, 1) = '*'">
        <!-- output diacritics+base -->
        <xsl:value-of select="$a_state"/>
        <xsl:value-of select="$a_char"/>
      </xsl:when>

      <!-- if lower letter -->
      <xsl:otherwise>
        <!-- output base+diacritics -->
        <xsl:value-of select="$a_char"/>
        <xsl:value-of select="$a_state"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


<!--
    Test whether text is in betacode
    Parameters:
      $a_in         string/node to be tested
    Output:
      1 if encoded in betacode, else 0
    (Note: Boolean return value does not seem to work
    reliably, perhaps because of recursion.)
  -->
  <xsl:template name="is-beta">
    <xsl:param name="a_in"/>

    <xsl:choose>
      <!-- if xml:lang says betacode, so be it -->
      <xsl:when test="lang('grc-x-beta')">
        <xsl:value-of select="1"/>
      </xsl:when>

      <!-- if no input, can't be betacode -->
      <xsl:when test="string-length($a_in) = 0">
        <xsl:value-of select="0"/>
      </xsl:when>

      <!-- otherwise, check the characters in input -->
      <xsl:otherwise>
        <xsl:variable name="head" select="substring($a_in, 1, 1)"/>

        <xsl:choose>
          <!-- if betacode base letter, assume it's betacode -->
          <xsl:when
            test="contains($s_betaUppers, $head) or
                  contains($s_betaLowers, $head)">
            <xsl:value-of select="1"/>
          </xsl:when>

          <xsl:otherwise>
            <!-- look up unicode in table -->
            <xsl:variable name="beta">
              <xsl:apply-templates select="$s_betaUniTable" mode="u2b">
                <xsl:with-param name="a_key" select="$head"/>
              </xsl:apply-templates>
            </xsl:variable>

            <xsl:choose>
              <!-- if found in unicode table, it's not betacode -->
              <xsl:when test="string-length($beta) > 0">
                <xsl:value-of select="0"/>
              </xsl:when>

              <!-- otherwise, skip letter and check remainder of string -->
              <xsl:otherwise>
                <xsl:call-template name="is-beta">
                  <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Convert Greek betacode to Unicode
    Parameters:
      $a_in           betacode input string to be converted
      $a_pending      character waiting to be output
      $a_state        diacritics associated with pending character
      $a_precomposed  whether to put out precomposed or decomposed Unicode
      $a_partial      whether this is a partial word
                      (If true, do not use final sigma for last letter)

    Output:
      $a_in transformed to equivalent Unicode

    The characters in the state string are maintained in a canonical order,
    which allows the lookup table to contain a single entry for each
    combination of base character and diacritics.  The diacritics may appear
    in any order in the input.

    Diacritics associated with (either preceding or following) a base
    character are accumulated until either a non-diacritic character or end
    of input are encountered, at which point the pending character is output.
  -->
  <xsl:template name="beta-to-uni">
    <xsl:param name="a_in"/>
    <xsl:param name="a_pending" select="''"/>
    <xsl:param name="a_state" select="''"/>
    <xsl:param name="a_precomposed" select="true()"/>
    <xsl:param name="a_partial" select="false()"/>

    <xsl:variable name="head" select="substring($a_in, 1, 1)"/>

    <xsl:choose>
      <!-- if no more input -->
      <xsl:when test="string-length($a_in) = 0">
        <!-- output last pending char -->
        <xsl:choose>
          <!-- final sigma: S with no state -->
          <xsl:when
            test="(($a_pending = 's') or ($a_pending = 'S')) and
                  not($a_partial) and (string-length($a_state) = 0)">
            <xsl:call-template name="output-uni-char">
              <xsl:with-param name="a_char" select="$a_pending"/>
              <xsl:with-param name="a_state" select="'2'"/>
              <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
            </xsl:call-template>
          </xsl:when>

          <xsl:otherwise>
            <xsl:call-template name="output-uni-char">
              <xsl:with-param name="a_char" select="$a_pending"/>
              <xsl:with-param name="a_state" select="$a_state"/>
              <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- if input starts with "*" -->
      <xsl:when test="$head = '*'">
        <!-- output pending char -->
        <xsl:call-template name="output-uni-char">
          <xsl:with-param name="a_char" select="$a_pending"/>
          <xsl:with-param name="a_state" select="$a_state"/>
          <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
        </xsl:call-template>

        <!-- recurse, capitalizing next char, erasing any saved state -->
        <xsl:call-template name="beta-to-uni">
          <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
          <xsl:with-param name="a_state" select="'*'"/>
          <xsl:with-param name="a_pending" select="''"/>
          <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
          <xsl:with-param name="a_partial" select="$a_partial"/>
        </xsl:call-template>
      </xsl:when>

      <!-- if input starts with diacritic -->
      <xsl:when test="contains($s_betaDiacritics, $head)">
        <!-- update state with new character -->
        <xsl:variable name="newstate">
          <xsl:call-template name="insert-diacritic">
            <xsl:with-param name="a_string" select="$a_state"/>
            <xsl:with-param name="a_char" select="$head"/>
          </xsl:call-template>
        </xsl:variable>

        <!-- recurse with updated state -->
        <xsl:call-template name="beta-to-uni">
          <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
          <xsl:with-param name="a_state" select="$newstate"/>
          <xsl:with-param name="a_pending" select="$a_pending"/>
          <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
          <xsl:with-param name="a_partial" select="$a_partial"/>
        </xsl:call-template>
      </xsl:when>

      <!-- if not special char -->
      <xsl:otherwise>
        <!-- output pending char -->
        <xsl:choose>
          <!-- final sigma: S with no state followed by word break -->
          <xsl:when
            test="(($a_pending = 's') or ($a_pending = 'S')) and
                  (string-length($a_state) = 0) and
                  (contains($s_betaSeparators, $head) or
                   contains($s_betaSeparators2, $head))">
            <xsl:call-template name="output-uni-char">
              <xsl:with-param name="a_char" select="$a_pending"/>
              <xsl:with-param name="a_state" select="'2'"/>
              <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
            </xsl:call-template>
          </xsl:when>

          <xsl:otherwise>
            <xsl:call-template name="output-uni-char">
              <xsl:with-param name="a_char" select="$a_pending"/>
              <xsl:with-param name="a_state" select="$a_state"/>
              <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>

        <!-- reset state if there was a pending character -->
        <xsl:variable name="newstate">
          <xsl:choose>
            <xsl:when test="$a_pending"/>
            <xsl:otherwise>
              <xsl:value-of select="$a_state"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <!-- recurse with head as pending char -->
        <xsl:call-template name="beta-to-uni">
          <xsl:with-param name="a_in" select="substring($a_in, 2)"/>
          <xsl:with-param name="a_state" select="$newstate"/>
          <xsl:with-param name="a_pending" select="$head"/>
          <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
          <xsl:with-param name="a_partial" select="$a_partial"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Output a single character with diacritics
    Parameters:
      $a_char         character to be output
      $a_state        diacritics associated with character
      $a_precomposed  whether to put out precomposed or decomposed Unicode
  -->
  <xsl:template name="output-uni-char">
    <xsl:param name="a_char"/>
    <xsl:param name="a_state"/>
    <xsl:param name="a_precomposed"/>

    <xsl:choose>
      <!-- if no character pending -->
      <xsl:when test="string-length($a_char) = 0">
        <!-- if we have state and we're not processing a capital -->
        <xsl:if
          test="(string-length($a_state) > 0) and
                      (substring($a_state, 1, 1) != '*')">
          <!-- output just the state -->
          <!-- here precomposed=true means don't make it combining -->
          <xsl:apply-templates select="$s_betaUniTable" mode="b2u">
            <xsl:with-param name="a_key" select="$a_state"/>
            <xsl:with-param name="a_precomposed" select="true()"/>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:when>

      <!-- if character is pending -->
      <xsl:otherwise>
        <!-- translate to lower and back -->
        <xsl:variable name="lowerchar"
          select="translate($a_char, $s_betaUppers, $s_betaLowers)"/>
        <xsl:variable name="upperchar"
          select="translate($a_char, $s_betaLowers, $s_betaUppers)"/>
        <xsl:choose>
          <!-- if upper != lower, we have a letter -->
          <xsl:when test="$lowerchar != $upperchar">
            <!-- use letter+state as key into table -->
            <xsl:apply-templates select="$s_betaUniTable" mode="b2u">
              <xsl:with-param name="a_key"
                              select="concat($lowerchar, $a_state)"/>
              <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
            </xsl:apply-templates>
          </xsl:when>

          <!-- if upper = lower, we have a non-letter -->
          <xsl:otherwise>
            <!-- output character, if any, then use state as key into table -->
            <!-- this handles the case of isolated diacritics -->
            <xsl:value-of select="$a_char"/>
            <xsl:if test="string-length($a_state) > 0">
              <xsl:apply-templates select="$s_betaUniTable" mode="b2u">
                <xsl:with-param name="a_key" select="$a_state"/>
                <xsl:with-param name="a_precomposed" select="$a_precomposed"/>
              </xsl:apply-templates>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
