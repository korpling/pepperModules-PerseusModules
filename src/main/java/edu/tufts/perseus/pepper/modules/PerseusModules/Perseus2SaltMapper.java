/**
 * Copyright 2009 TUFTS university.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package edu.tufts.perseus.pepper.modules.PerseusModules;

import java.lang.String;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.osgi.service.log.LogService;

import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
//import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SProcessingAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SProcessingAnnotation;
import edu.tufts.perseus.pepper.modules.PerseusModules.exceptions.PerseusImporterException;

public class Perseus2SaltMapper extends DefaultHandler  
{
	private URI resourcesURI= null;
	
	/**
	 * Sets the {@link URI}, where to find the resources needed by this mapper. 
	 * @param resourcesURI uri of resources
	 */
	public void setResourcesURI(URI resourcesURI) {
		this.resourcesURI = resourcesURI;
	}

	/**
	 * Returns the {@link URI}, where to find the resources needed by this mapper.
	 * @return uri of resources
	 */
	public URI getResourcesURI() {
		return resourcesURI;
	}
	
	private LogService logService= null;
	/**
	 * Set the Log Service
	 * @param a_log a {@link LogService} object which with should be logged
	 */
	public void setLogService(LogService a_log)
	{
		this.logService = a_log;
	}
	
	/**
	 * Returns the Log Service
	 * @return current {@link LogService} object
	 */
	public LogService getLogService()
	{
		return(this.logService);
	}

	
//	private SCorpus sCorpus = null;
	private ArrayList<SDocument> docList = null;
	private SDocument sDocument= null;
	private STextualDS sTextDS = null;
	private SStructure currentSentence = null;	
	private HashMap<String,String> currentAnnotatorInfo = null;
	private String currentText = null;			
	private HashMap<String,SToken> tokenMap = null;
	private HashMap<String,SStructure> ssMap = null;
	private HashMap<String,ArrayList<HashMap<String,String>>> tokenRelMap = null;
	private HashMap<String,String> allAnnotators = null;

//	private String KW_TOKENSEP="salt.tokenSeperator";	
	public static final String DEFAULT_SEPARATOR= " ";
	private String docLang = "";
	private StreamSource xsltSource;
	
	public static final String FILE_ALPHEIOS_XSL="alpheios-beta2unicode.xsl";
	public static final String FILE_DUMMY_XSL="dummy.xml";
//	private SSpan currentSpan;
//	private SToken currentSentenceToken;

	
	public Perseus2SaltMapper() {
		super();
		this.initCorpus();
	}

	private void initCorpus()
	{
		this.allAnnotators = new HashMap<String,String>();
		this.docList = new ArrayList<SDocument>();		
	}
	
	private static final HashMap<String,Integer> postagMap = 
		new HashMap<String,Integer>()
		{
		private static final long serialVersionUID = -5308305950463962194L;
		{
			put(IConstants.ANN_PERS,new Integer(1));			
			put(IConstants.ANN_NUM,new Integer(2));
			put(IConstants.ANN_TENSE,new Integer(3));
			put(IConstants.ANN_MOOD,new Integer(4));
			put(IConstants.ANN_VOICE,new Integer(5));
			put(IConstants.ANN_GEND,new Integer(6));
			put(IConstants.ANN_CASE,new Integer(7));
			put(IConstants.ANN_DEG,new Integer(8));
			put(IConstants.ANN_POSS,new Integer(9));
			put(IConstants.ANN_DEF,new Integer(10));
			put(IConstants.ANN_OBJ,new Integer(11));
			put(IConstants.ANN_PFX,new Integer(12));
			put(IConstants.ANN_SORT,new Integer(13));
		}};
	
	private static final HashMap<String,HashMap> abbrevMap 
		= new HashMap<String,HashMap>()
		{{
			put (IConstants.ANN_POFS, new HashMap<String,String>()
				{{ put("n",IConstants.POFS_NOUN);
				   put("v",IConstants.POFS_VERB);
				   put("t",IConstants.POFS_PARTICIPLE);		
				   put("a",IConstants.POFS_ADJECTIVE);
				   put("d",IConstants.POFS_ADVERB);
				   put("c",IConstants.POFS_CONJUNCTION);
				   put("l",IConstants.POFS_ARTICLE);
					put("g",IConstants.POFS_PARTICLE);
					put("r",IConstants.POFS_PREPOSITION);
					put("p",IConstants.POFS_PRONOUN);
					put("m",IConstants.POFS_NUMERAL);
					put("i",IConstants.POFS_INTERJECTION);
					put("e",IConstants.POFS_EXCLAMATION);
					put("x",IConstants.POFS_IRREGULAR);
					put("u",IConstants.POFS_PUNCTUATION);
					put("f",IConstants.POFS_FUNCTIONAL);
					put("z",IConstants.POFS_DETERMINER);
				}});
			put (IConstants.ANN_PERS, new HashMap<String,String>()
				{{ 
					put("1",IConstants.PERS_FIRST);
					put("2",IConstants.PERS_SECOND);
					put("3",IConstants.PERS_THIRD);					
				}});
			put (IConstants.ANN_NUM, new HashMap<String,String>()
				{{ 
					put("s",IConstants.NUM_SING);
					put("p",IConstants.NUM_PLURAL);
					put("d",IConstants.NUM_DUAL);
				}});
			put (IConstants.ANN_TENSE, new HashMap<String,String>()
				{{ 
					put("p",IConstants.TENSE_PRESENT);
					put("i",IConstants.TENSE_IMPERFECT);
					put("r",IConstants.TENSE_PERFECT);
					put("l",IConstants.TENSE_PLUPERFECT);
					put("t",IConstants.TENSE_FUTUREPERFECT);
					put("f",IConstants.TENSE_FUTURE);
					put("a",IConstants.TENSE_AORIST);
					put("e",IConstants.TENSE_PRETERITE);
					put("c",IConstants.TENSE_CONDITIONAL);
				}});	
			put (IConstants.ANN_MOOD, new HashMap<String,String>()
				{{ 
					put("i",IConstants.MOOD_IND);
					put("s",IConstants.MOOD_SUBJ);
					put("o",IConstants.MOOD_OPT);
					put("n",IConstants.MOOD_INF);
					put("m",IConstants.MOOD_IMP);
					put("g",IConstants.MOOD_GERUND);
					put("p",IConstants.MOOD_PART);
				}});
			put (IConstants.ANN_VOICE, new HashMap<String,String>()
				{{ 
					put("m",IConstants.VOICE_MIDDLE);
					put("a",IConstants.VOICE_ACTIVE);
					put("p",IConstants.VOICE_PASSIVE);
					put("d",IConstants.VOICE_DEP);
					put("e",IConstants.VOICE_MEDIOPASS);					
				}});					
			put (IConstants.ANN_GEND, new HashMap<String,String>()
				{{ 
					put("m",IConstants.GEND_MASC);
					put("f",IConstants.GEND_FEM);
					put("n",IConstants.GEND_NEUT);
					put("c",IConstants.GEND_COM);
				}});
			put (IConstants.ANN_CASE, new HashMap<String,String>()
				{{ 
					put("n",IConstants.CASE_NOM);
					put("g",IConstants.CASE_GEN);
					put("d",IConstants.CASE_DAT);
					put("a",IConstants.CASE_ACC);
					put("b",IConstants.CASE_ABL);
					put("v",IConstants.CASE_VOC);
					put("i",IConstants.CASE_INS);
					put("l",IConstants.CASE_LOC);
					
				}});			
			put (IConstants.ANN_DEG, new HashMap<String,String>()
				{{ 
					put("p",IConstants.DEG_POS);
					put("c",IConstants.DEG_COMP);
					put("s",IConstants.DEG_SUP);
				}});				
			put (IConstants.ANN_POSS, new HashMap<String,String>()
				{{ 
					put("a",IConstants.POSS_FP);
					put("b",IConstants.POSS_FS);
					put("c",IConstants.POSS_SD);
					put("d",IConstants.POSS_SFP);
					put("e",IConstants.POSS_SFS);
					put("f",IConstants.POSS_SMP);
					put("g",IConstants.POSS_SMS);
					put("h",IConstants.POSS_TD);
					put("i",IConstants.POSS_TFP);
					put("j",IConstants.POSS_TFS);
					put("k",IConstants.POSS_TMP);
					put("l",IConstants.POSS_TMS);
															
				}});				
			put (IConstants.ANN_DEF, new HashMap<String,String>()
				{{ 
					put("d",IConstants.DEF_DEF);
					put("i",IConstants.DEF_IND);
					
				}});
			put (IConstants.ANN_OBJ, new HashMap<String,String>()
				{{ 
					put("a",IConstants.OBJ_FP);
					put("b",IConstants.OBJ_FS);
					put("c",IConstants.OBJ_SD);
					put("d",IConstants.OBJ_SFP);
					put("e",IConstants.OBJ_SFS);
					put("f",IConstants.OBJ_SMP);
					put("g",IConstants.OBJ_SMS);
					put("h",IConstants.OBJ_TD);
					put("i",IConstants.OBJ_TFP);
					put("j",IConstants.OBJ_TFS);
					put("k",IConstants.OBJ_TMP);
					put("l",IConstants.OBJ_TMS);
					
				}});				
			put (IConstants.ANN_PFX, new HashMap<String,String>()
				{{ 
					put("c",IConstants.PFX_CONJ);
					put("e",IConstants.PFX_EMP);
					put("i",IConstants.PFX_INT);
					put("n",IConstants.PFX_NEG);
					put("p",IConstants.PFX_PREP);
					put("r",IConstants.PFX_RES);										
				}});
			put (IConstants.ANN_SORT, new HashMap<String,String>()
				{{ 
					put("c",IConstants.SORT_CARD);
					put("o",IConstants.SORT_ORD);
				}});
																		
		}};
	
	public void setCorpus(SCorpus a_sCorpus)
	{						
		this.addSMetaAnnotationString(
			a_sCorpus, IConstants.ANN_CORPUS_NAME, a_sCorpus.getSName());
		Iterator<SDocument> dIter = this.docList.iterator();
		int num = 0;
		while (dIter.hasNext())
		{
			SDocument doc = dIter.next();
			this.addSMetaAnnotationString(a_sCorpus, IConstants.ANN_CORPUS_DOC+(++num), this.getDocName(doc));
			// TODO - eventually add the document titles here too
		}
		Iterator<String> iter = this.allAnnotators.keySet().iterator();
		while (iter.hasNext())
		{
			String key = iter.next();
			String value = this.allAnnotators.get(key);
			this.addSMetaAnnotationString(a_sCorpus, IConstants.ANN_CORPUS_ANNOTATOR +  key, value);			
		}					
		this.initCorpus();
	}
	
	/**
	 * SAX Handler Methods
	 */
	@Override
	public void startDocument() throws SAXException {
		//System.out.println("Starting document");
	
	}
		
	@Override
	public void endDocument() throws SAXException {
		//System.out.println("Ending document");
	}

	@Override
	public void startElement(
			String a_uri, 
			String a_localName, 
			String a_qName, Attributes a_atts) 
		throws SAXException
	{
		
		this.currentText = "";
		if (a_qName.equals("treebank"))
		{
			// create layer for syntactic annotation
			// handle attributes: 
			// schema
			// lang
			// date
			this.docLang = a_atts.getValue("xml:lang");
			// TODO externalize location of xslt
			if (this.docLang != null && this.docLang.equals("grc"))
			{				
//				this.xsltSource = new StreamSource(new File("resources/alpheios-beta2unicode.xsl"));
				if (this.getResourcesURI()== null)
					throw new PerseusImporterException("Cannot import data, because the resource folder is not set.");
				File xsltFile= new File(this.getResourcesURI().toFileString()+"/"+FILE_ALPHEIOS_XSL);
				
				if (!xsltFile.exists())
					throw new PerseusImporterException("Cannot import data, because a necessary resource file '"+xsltFile.getAbsolutePath()+"' does not exist.");
				this.xsltSource = new StreamSource(xsltFile);
				
			}
		}			
		else if (a_qName.equals("annotator") && this.currentSentence == null)
		{
			this.currentAnnotatorInfo = new HashMap<String,String>();
		}
		else if (a_qName.equals("sentence"))
		{
			this.startSentence(a_atts);
		}
		else if (a_qName.equals("word"))
		{
			this.startWord(a_atts);
		}
	}

	@Override
	public void endElement(
			String a_uri, 
			String a_localName, 
			String a_qName) 
		throws SAXException 
	{

		if ("date".equals(a_qName))
		{
			java.text.DateFormat df = new java.text.SimpleDateFormat(IConstants.DATE_FORMAT);
			try {				
				// TODO add metadata annotation to layer instead of document
				this.addDate(this.getDocument(), df.parse(this.currentText));
			} catch (ParseException a_e)
			{
				if (this.getLogService()!= null)
					this.getLogService().log(LogService.LOG_WARNING, "Unable to parse date " + currentText + ":",a_e);
//				throw new SAXException("Unable to parse date " + currentText + ":",a_e);
			} catch (Exception e) {
				if (this.getLogService()!= null)
					this.getLogService().log(LogService.LOG_WARNING, "Unable to parse date " + currentText + ":",e);
			}
		
		}
		else if (a_qName.equals("annotator") && this.currentSentence == null)
		{
		
			// if there is no current sentence, it should be a new document level annotator
			this.addAnnotator(this.currentAnnotatorInfo);			
			// TODO - add to layer rather than document?
		}
		else if (this.currentSentence == null && (
				a_qName.equals(IConstants.ATT_ANNOTATOR_SHORT) || 
				 a_qName.equals(IConstants.ATT_ANNOTATOR_NAME) || 
				 a_qName.equals(IConstants.ATT_ANNOTATOR_ADDRESS)))
		{
			this.currentAnnotatorInfo.put(a_qName,this.currentText);
		}
		else if (a_qName.equals(IConstants.ATT_ANNOTATOR_PRIMARY) || 
				 a_qName.equals(IConstants.ATT_ANNOTATOR_SECONDARY) ||
				 (a_qName.equals("annotator") && this.currentSentence != null))
		{
			String shortname = IConstants.ANN_ANNOTATOR;
			if (! a_qName.equals("annotator"))
			{
				shortname = shortname + "_" + a_qName;
	
			}
			String fullname = IConstants.NS + 
			  "::" + shortname; 
			  
			SAnnotation ann = this.currentSentence.getSAnnotation(fullname);
			if (this.currentSentence != null)
			{
				if (ann != null)
				{
					ann.setSValue(ann.getSValue() + "," + this.currentText);												
				}
				else 
				{					
					ann = this.addSAnnotationString(this.currentSentence,
							shortname, this.currentText);
				}
			}
		}
		else if (a_qName.equals("sentence"))
		{
			this.endSentence();
		}
	}
	
	@Override	
	public void characters(char a_ch[], int a_start, int a_length)
		throws SAXException
	{
		this.currentText = this.currentText + new String(a_ch, a_start, a_length);
	}


	/**********************************************************
	 * SALT Model Accessors 
	 * 
	 **********************************************************/
		
	/**
	 * initialize for a new document
	 */
	private void initDocument()
	{
		this.currentSentence = null;
//		this.currentSentenceToken = null;
//		this.currentSpan = null;
		this.currentText = null;
		this.currentAnnotatorInfo = null;
		this.sDocument = null;				
	}
	
	/**
	 * Set the SDocument
	 */
	public void setDocument(SDocument a_sDocument) {
		this.initDocument();
		this.sDocument = a_sDocument;
		this.docList.add(a_sDocument);
		this.log(LogService.LOG_DEBUG, "Creating DocumentGraph for " + this.getDocument().getSId());
		this.sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		String name = this.getDocName(a_sDocument);
		this.sDocument.getSDocumentGraph().setSName(name);
		this.sDocument.getSDocumentGraph().setSId(this.getDocument().getSId());
	
		this.log(LogService.LOG_DEBUG, "Adding TextDS");
		// TODO not sure if we want to associate a primary text element or not...
		STextualDS sTextDS= SaltFactory.eINSTANCE.createSTextualDS();
		this.setText(sTextDS);
		this.sDocument.getSDocumentGraph().addSNode(sTextDS);
		//this.addSMetaAnnotationString(
		//	this.getDocument(), IConstants.ATT_DOCID, this.getDocument().getSId());
		this.addSMetaAnnotationString(
			this.getDocument(), 
			IConstants.ANN_DOCUMENT_ID, 
			name);	
	}
	
	/**
	 * Get the SDocument
	 * @return
	 */
	public SDocument getDocument() {
		return sDocument;
	}
	
	/**
	 * Set the STextDS
	 * @param a_sTextDS
	 */
	public void setText(STextualDS a_sTextDS) {
		this.sTextDS = a_sTextDS;
	}
	
	/**
	 * Get the STextDS
	 * @return
	 */
	public STextualDS getTextDS() {
		return this.sTextDS;
	}

	/**************************************************************************************
	 *  Perseus Treebank to Salt MetaModel Mapping Methods	
	 **************************************************************************************/
	
	/**
	 * Add the Date as an SMetaAnnotation 
	 * @param a_elem the element to add it to
	 * @param a_date the date object
	 */
	private void addDate(SMetaAnnotatableElement a_elem,Date a_date)
	{
		SMetaAnnotation ann = SaltFactory.eINSTANCE.createSMetaAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setSName(IConstants.ANN_DATE);
		ann.setValue(a_date);
		a_elem.addSMetaAnnotation(ann);
	}
	
		
	/**
	 * Create an Annotator MetaAnnotation
	 * @param a_info the accumulated data for the Annotator
	 * @return a String key to access the MetaAnnotation by
	 */
	private void addAnnotator(Map a_info)
	{		
		String key = (String)a_info.get(IConstants.ATT_ANNOTATOR_SHORT);
		String nameStr = (String)a_info.get(IConstants.ATT_ANNOTATOR_NAME);
		String addyStr = (String)a_info.get(IConstants.ATT_ANNOTATOR_ADDRESS);
		
		HashMap<String,String> ann = new HashMap<String,String>();
		this.allAnnotators.put(key, nameStr + "," + addyStr);
	}
			
	
	private String getLongString(String a_att, String a_value)
	{
		return (String)(this.abbrevMap.get(a_att).get(a_value));
	}
	
	/**
	 * Maps a Perseus word node to an SToken and adds 
	 * - relationships between the word SToken and the current sentence SStructure 
	 * - syntactic annotations on the word
 	 * Dependency relationships between the word and other words in the sentence are stored for 
 	 * processing at the end of the sentence
	 * @param a_atts the attributes of the word node
	 */
	private void startWord(Attributes a_atts)
	{		
		

		this.log(LogService.LOG_DEBUG, "Adding Word SStructure & Token");
		SStructure sStructure= SaltFactory.eINSTANCE.createSStructure();
		this.getDocument().getSDocumentGraph().addSNode(sStructure);		
		SAnnotation ssAnno= SaltFactory.eINSTANCE.createSCatAnnotation();
		ssAnno.setSValue(IConstants.ANN_WORD);
		ssAnno.setNamespace(IConstants.NS);
		sStructure.addSAnnotation(ssAnno);
		
		SToken sToken= SaltFactory.eINSTANCE.createSToken();
		
		// TODO add the word to the layer instead of document?
		this.getDocument().getSDocumentGraph().addSNode(sToken);
		
		// Add the dominance relationship between the word token and the sStructure for it
		SDominanceRelation sDomRel= SaltFactory.eINSTANCE.createSDominanceRelation();		
		sDomRel.setSStructure(sStructure);
		sDomRel.setSTarget(sToken);
		SAnnotation dAnno= SaltFactory.eINSTANCE.createSAnnotation();
		dAnno.setNamespace(IConstants.NS);
		dAnno.setSName(IConstants.ANN_RELATION);
		dAnno.setSValue(IConstants.ANN_WORD_TOKEN);
		sDomRel.addSAnnotation(dAnno);
		this.getDocument().getSDocumentGraph().addSRelation(sDomRel);		
		
		// Add a relationship between the word token and the sentence
		//SSpanningRelation sSpanRel = SaltFactory.eINSTANCE.createSSpanningRelation();
		//sSpanRel.setSSpan(this.currentSpan);
		//sSpanRel.setTarget(sToken);						
		//this.getDocument().getSDocumentGraph().addSRelation(sSpanRel);
		//this.addSAnnotationString(sSpanRel, IConstants.ANN_RELATION, IConstants.ANN_RELATION_SPAN);		
		
		String postag = a_atts.getValue(IConstants.ATT_POSTAG);
		String lemmaFull = a_atts.getValue(IConstants.ATT_LEMMA);		
		String lemma = lemmaFull.replaceAll("[0-9]*$", "");
		String sense = lemmaFull.replaceAll("^[^0-9]+", "");
		String form = a_atts.getValue(IConstants.ATT_FORM);
		String relation = a_atts.getValue(IConstants.ATT_RELATION);
		String id = a_atts.getValue(IConstants.ATT_ID);
		String cid = a_atts.getValue(IConstants.ATT_CID);
		String head = a_atts.getValue(IConstants.ATT_HEAD);
		
		// TODO create CTS URN to word using id
		this.addSAnnotationString(sToken, IConstants.ANN_WORD_ID, cid);
		
		if (postag != null && !(postag.equalsIgnoreCase("")))
		{
			this.log(LogService.LOG_DEBUG, "Adding POSTAG annotation");
			// Add as-is as annotation on token
			SAnnotation sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setNamespace(IConstants.NS);
			sAnno.setName(IConstants.ATT_POSTAG);
			sAnno.setSValue(postag);
			sToken.addSAnnotation(sAnno);
			
			// add components of pofstag separate annotations
			SAnnotation pAnno= SaltFactory.eINSTANCE.createSPOSAnnotation();			
			String pos = postag.substring(0, 1);
			if (!pos.equals(IConstants.EMPTY))
			{
				this.log(LogService.LOG_DEBUG, "Adding POFS annotation");
				pAnno.setSValue(this.getLongString(IConstants.ANN_POFS,pos));
				sToken.addSAnnotation(pAnno);
			}
			Iterator<String> iter = postagMap.keySet().iterator();
			while (iter.hasNext())
			{
			
				String key = iter.next();
				
				int index = postagMap.get(key).intValue();
				//this.log(LogService.LOG_DEBUG, "Postag piece: " + key + "=" + index);
				String value = null;
				try {
					value = postag.substring(index,index+1);
				} catch (java.lang.StringIndexOutOfBoundsException a_e) {
					// no more parts to postag
				}
				if (value != null && ! value.equals(IConstants.EMPTY))
				{
					this.log(LogService.LOG_DEBUG, "Adding annotation: " + key + "=" + value);
					sAnno = SaltFactory.eINSTANCE.createSAnnotation();				
					sAnno.setNamespace(IConstants.NS);
					sAnno.setName(key);				
					sAnno.setSValue(this.getLongString(key,value));
					sToken.addSAnnotation(sAnno);
				}
			}									
		}

		if (lemma != null && ! lemma.isEmpty())
		{
			
			SAnnotation sAnno = SaltFactory.eINSTANCE.createSLemmaAnnotation();	
			sAnno.setSValue(this.transformText(lemma));
			sAnno.setNamespace(IConstants.NS);
			sToken.addSAnnotation(sAnno); 
			this.log(LogService.LOG_DEBUG, "Added Lemma Annotation " + sAnno.getSValueSTEXT());
			
			if (this.docLang != null && this.docLang.equals("grc"))
			{
				// TODO only if greek beta
				SAnnotation bAnno = SaltFactory.eINSTANCE.createSLemmaAnnotation();	
				bAnno.setSValue(lemma.replaceAll("\\\\", java.util.regex.Matcher.quoteReplacement("\\\\")));
				bAnno.setNamespace(IConstants.NS);
				bAnno.setName(IConstants.ANN_LEMMA_BETA);
				sToken.addSAnnotation(bAnno); 
				this.log(LogService.LOG_DEBUG, "Added Lemma-Beta Annotation " + bAnno.getSValueSTEXT());
			}
		}
		
		if (sense != null && ! sense.isEmpty())
		{
			this.addSAnnotationString(sToken, IConstants.ANN_SENSE, sense);
		}
		
		if (form != null && ! form.isEmpty())
		{
			this.log(LogService.LOG_DEBUG, "Adding Form Annotation");
			SAnnotation sAnno = SaltFactory.eINSTANCE.createSAnnotation();														
			sAnno.setSValue(this.transformText(form));
			sAnno.setNamespace(IConstants.NS);
			sAnno.setName(IConstants.ATT_FORM);
			sToken.addSAnnotation(sAnno); 
			
			if (this.docLang != null && this.docLang.equals("grc"))
			{
				SAnnotation bAnno = SaltFactory.eINSTANCE.createSAnnotation();								
							
				bAnno.setSValue(form.replaceAll("\\\\", java.util.regex.Matcher.quoteReplacement("\\\\")));
				bAnno.setNamespace(IConstants.NS);
				bAnno.setName(IConstants.ANN_FORM_BETA);
				sToken.addSAnnotation(bAnno); 
			}
		}
		
		// store the token, head and relation
		this.ssMap.put(id, sStructure);
		this.tokenMap.put(id, sToken);
		
		this.log(LogService.LOG_DEBUG, "Linking root word to sentence " + this.currentSentence.getSId());
		// add dominance relationship between the word and the sentence
		SDominanceRelation tDomRel= SaltFactory.eINSTANCE.createSDominanceRelation();		
		tDomRel.setSStructure(this.currentSentence);
		tDomRel.setSTarget(sStructure);
		SAnnotation sAnno= SaltFactory.eINSTANCE.createSAnnotation();
		sAnno.setNamespace(IConstants.NS);
		sAnno.setSName(IConstants.ANN_RELATION);
		sAnno.setSValue(relation);
		tDomRel.addSAnnotation(sAnno);
		this.getDocument().getSDocumentGraph().addSRelation(tDomRel);
		this.log(LogService.LOG_DEBUG, "Linked root word to sentence " + this.currentSentence.getSId());
		
		if (!(head.equals("0")))
		{
			this.saveDependency(head, id, relation);
		}
		//create an empty text, if there is none
		if (this.getTextDS().getSText()== null)
			this.getTextDS().setSText("");

		
		//if text of token isn't empty put a separator to its end

		this.getTextDS().setSText(this.getTextDS().getSText()+ this.DEFAULT_SEPARATOR);	
		

		
		Integer startPos= 0;
		Integer endPos= 0;
		
		//set startpos to current text length
		startPos= this.getTextDS().getSText().length();
		this.getTextDS().setSText(this.getTextDS().getSText()+ this.transformText(form));
		endPos= this.getTextDS().getSText().length();				 
		
		//create STextualRelation
		STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSTextualDS(this.getTextDS());
		sTextRel.setSToken(sToken);
		sTextRel.setSStart(startPos);
		sTextRel.setSEnd(endPos);		
		this.getDocument().getSDocumentGraph().addSRelation(sTextRel);
		
	}
		
	/**
	 * Maps all sentences to SSTructures.
	 * @param nts
	 */
	private void startSentence(Attributes a_atts)
	{	
		this.log(LogService.LOG_DEBUG, "Adding Sentence " + a_atts.getValue("id"));
		SStructure sStructure= SaltFactory.eINSTANCE.createSStructure();
		this.getDocument().getSDocumentGraph().addSNode(sStructure);
		this.currentSentence = sStructure;		
	
		SAnnotation sAnno= SaltFactory.eINSTANCE.createSCatAnnotation();
		sAnno.setNamespace(IConstants.NS);
		sAnno.setSValue(IConstants.ANN_SENTENCE);
		sStructure.addSAnnotation(sAnno);	
				
		String docId = a_atts.getValue(IConstants.ATT_DOCID);
		String subDoc = a_atts.getValue(IConstants.ATT_SUBDOC);
		String span = a_atts.getValue(IConstants.ATT_SPAN);
		String id = a_atts.getValue(IConstants.ATT_ID);

		
		//SSpan sSpan = SaltFactory.eINSTANCE.createSSpan();
		//this.getDocument().getSDocumentGraph().addSNode(sSpan);
		//this.currentSpan = sSpan;
		//this.addSAnnotationString(sSpan,IConstants.ANN_SENTENCESPAN_ID,id);
		
		
		// TODO use CTS URN 
		
		String[] parts = subDoc.split(":");
		ArrayList<String> chunkList = new ArrayList<String>(); 
		for (int i=0; i<parts.length; i++)
		{
			String[] pair = parts[i].split("=");
			if (pair.length == 2)
			{
				chunkList.add(pair[1]);
			}
		}
		String chunkId = docId;
		Iterator<String> iter = chunkList.iterator();
		while (iter.hasNext())
		{ 
			chunkId = chunkId + "." + iter.next();
		}		 
		this.addSAnnotationString(sStructure, IConstants.ANN_SENTENCE_ID, 
				id);
		this.addSAnnotationString(sStructure, IConstants.ANN_SUBDOC, 
				chunkId);
		this.addSAnnotationString(sStructure, IConstants.ANN_SPAN, 
				span.replaceAll("\\\\", java.util.regex.Matcher.quoteReplacement("\\\\")));
		
								
		// TODO add id as meta annotation
		tokenMap = new HashMap<String,SToken>();
		ssMap = new HashMap<String,SStructure>();
		tokenRelMap = new HashMap<String,ArrayList<HashMap<String,String>>>();														
	}
	
	/**
	 * Add dependency relationships between the word tokens in the Sentence
	 */
	private void endSentence()
	{
		Iterator<String> iter = tokenMap.keySet().iterator();
		while (iter.hasNext())
		{
			String key = iter.next();
			this.setDependency(key);
		}		
	}
	
	private void saveDependency(String a_source, String a_target, String a_rel)
	{
		HashMap<String,String> relMap = new HashMap<String,String>();
		relMap.put(a_target, a_rel);
		if (! this.tokenRelMap.containsKey(a_source))
		{
			this.tokenRelMap.put(a_source,new ArrayList<HashMap<String,String>>());	
		}
		this.tokenRelMap.get(a_source).add(relMap);				 				
	}
	
	private void setDependency(String a_source)
	{		
		SToken parent = this.tokenMap.get(a_source);
		this.log(LogService.LOG_DEBUG, "Adding Token Dependency:" + a_source);	
		ArrayList<HashMap<String,String>> rels = this.tokenRelMap.get(a_source);
		if (rels != null)
		{
			Iterator<HashMap<String,String>> iter = rels.iterator();
			while (iter.hasNext())
			{
				HashMap<String,String> relMap = iter.next();
				Iterator<String> kiter = relMap.keySet().iterator();
				while (kiter.hasNext())
				{
					String key = kiter.next();
					SPointingRelation sRel= SaltFactory.eINSTANCE.createSPointingRelation();
					sRel.addSType(IConstants.ANN_REL_TYPE_PARENT);
					sRel.setSSource(parent);	
					this.getDocument().getSDocumentGraph().addSRelation(sRel);		
					this.log(LogService.LOG_DEBUG, "Adding Dependency Relation: " + (String)relMap.get(key) + " " + a_source + " to " + key + " node " + this.tokenMap.get(key));
					sRel.setSTarget((SToken)this.tokenMap.get(key));		
					SAnnotation sAnno= SaltFactory.eINSTANCE.createSAnnotation();
					sAnno.setNamespace(IConstants.NS);
					sAnno.setSName(IConstants.ANN_RELATION);
					sAnno.setSValue((String)relMap.get(key));
					sRel.addSAnnotation(sAnno);
				}
			}
		}
	}
	
	private void log(int a_level, String a_msg)
	{
		if (this.logService != null)
		{
			this.logService.log(a_level, a_msg);
		}
		else
		{
			System.err.println(a_msg);
		}
	}
	
	public static void main(String[] a_args)
	{
		String documentPath = a_args[0];
		Perseus2SaltMapper mapper= new Perseus2SaltMapper();	
		SDocument sdoc = SaltFactory.eINSTANCE.createSDocument();
		mapper.setDocument(sdoc);
		File resourceFolder= new File("./src/main/resources");
		mapper.setResourcesURI(URI.createFileURI(resourceFolder.getAbsolutePath()));
		  SAXParserFactory factory = SAXParserFactory.newInstance();			            
          try {
          	SAXParser parser = factory.newSAXParser();
	            parser.parse(documentPath,mapper);	
          } catch (Exception a_e)
          {
          	throw new PerseusImporterException(
          			"Unable to parse " + documentPath + ":",a_e);
          }	
	}
	
	/**
	 * If a transformation stylesheet has been provided
	 * for the document, transform the result accordingly
	 * @param a_text
	 * @return the transformed text
	 *         if no transformation is defined, the original
	 *         text is returned unchanged 
	 */
	private String transformText(String a_text)
	{
		String transformed;
		if (this.xsltSource != null)
		{
			StringWriter out = new StringWriter();
			Result result = 				
				new StreamResult(out);
			TransformerFactory transFact =
		        TransformerFactory.newInstance(  );
			try {		
	            
//				Source dummyXmlSource = new StreamSource(new File("resources/dummy.xml"));
				if (this.getResourcesURI()== null)	
					throw new PerseusImporterException("Cannot import data, because the resource folder is not set.");
				
				File dummyXmlFile= new File(this.getResourcesURI().toFileString()+"/"+FILE_DUMMY_XSL);
				if (!dummyXmlFile.exists())
					throw new PerseusImporterException("Cannot import data, because a necessary resource file '"+dummyXmlFile.getAbsolutePath()+"' does not exist.");
				Source dummyXmlSource = new StreamSource(dummyXmlFile);
				
			    Transformer trans =
				    transFact.newTransformer(this.xsltSource);
				trans.setParameter("e_in", a_text);
				trans.transform(dummyXmlSource,result);
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//this.log(LogService.LOG_DEBUG, "Transformed " + a_text + " to " + out);
			transformed = out.toString();
		}
		else
		{
			transformed = a_text;
		}
		return transformed;
	}
	
	private SMetaAnnotation addSMetaAnnotationString(
			SMetaAnnotatableElement a_elem,String a_name, String a_value)
	{
		SMetaAnnotation ann = SaltFactory.eINSTANCE.createSMetaAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setSName(a_name);
		ann.setSValue(a_value);
		a_elem.addSMetaAnnotation(ann);
		return ann;
	}
	
	private SAnnotation addSAnnotationString(
			SAnnotatableElement a_elem,String a_name, String a_value)
	{
		SAnnotation ann = SaltFactory.eINSTANCE.createSAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setSName(a_name);
		ann.setSValue(a_value);
		a_elem.addSAnnotation(ann);
		return ann;
	}
	
	private SProcessingAnnotation addSProcessingAnnotationString(
			SProcessingAnnotatableElement a_elem,String a_name, String a_value)
	{
		SProcessingAnnotation ann = SaltFactory.eINSTANCE.createSProcessingAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setSName(a_name);
		ann.setSValue(a_value);
		a_elem.addSProcessingAnnotation(ann);
		return ann;
	}
	
	private String getDocName(SDocument a_doc)
	{
		String name = "";
		URI uri = a_doc.getSElementPath();
		int segs = uri.segmentCount();
		if (segs > 1) // not a top level corpus
		{
			for (int i=1; i<segs; i++)
			{
				if (! name.isEmpty())
				{
					name = name + ".";
				}
				name = name + uri.segment(i);
			}
		}
		else 
		{ 
			name = uri.segment(0);
		}
		return name;
	}
}
