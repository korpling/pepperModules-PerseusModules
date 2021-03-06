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

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SProcessingAnnotation;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Perseus2SaltMapper extends DefaultHandler {
	private static final Logger logger = LoggerFactory.getLogger(Perseus2SaltMapper.class);

	private URI resourcesURI = null;

	/**
	 * Sets the {@link URI}, where to find the resources needed by this mapper.
	 * 
	 * @param resourcesURI
	 *            uri of resources
	 */
	public void setResourcesURI(URI resourcesURI) {
		this.resourcesURI = resourcesURI;
	}

	/**
	 * Returns the {@link URI}, where to find the resources needed by this
	 * mapper.
	 * 
	 * @return uri of resources
	 */
	public URI getResourcesURI() {
		return resourcesURI;
	}

	// private SCorpus sCorpus = null;
	private ArrayList<SDocument> docList = null;
	private SDocument sDocument = null;
	private STextualDS sTextDS = null;
	private SStructure currentSentence = null;
	private HashMap<String, String> currentAnnotatorInfo = null;
	private String currentText = null;
	private HashMap<String, SToken> tokenMap = null;
	private HashMap<String, ArrayList<HashMap<String, String>>> tokenRelMap = null;
	private HashMap<String, String> allAnnotators = null;

	// private String KW_TOKENSEP="salt.tokenSeperator";
	public static final String DEFAULT_SEPARATOR = " ";
	private StreamSource xsltSource;
	private String citation_base_uri = null;

	public static final String FILE_ALPHEIOS_XSL = "alpheios-beta2unicode.xsl";
	public static final String FILE_DUMMY_XSL = "dummy.xml";

	// private SSpan currentSpan;
	// private SToken currentSentenceToken;

	public Perseus2SaltMapper() {
		super();
		this.initCorpus();
	}

	private void initCorpus() {
		this.allAnnotators = new HashMap<String, String>();
		this.docList = new ArrayList<SDocument>();
	}

	/**
	 * Maps the character values of the POS tag components to their position in
	 * the 14 character @postag attribute
	 */
	private static final HashMap<String, Integer> postagMap = new HashMap<String, Integer>() {
		private static final long serialVersionUID = -5308305950463962194L;
		{
			put(IConstants.ANN_POFS, new Integer(0));
			put(IConstants.ANN_PERS, new Integer(1));
			put(IConstants.ANN_NUM, new Integer(2));
			put(IConstants.ANN_TENSE, new Integer(3));
			put(IConstants.ANN_MOOD, new Integer(4));
			put(IConstants.ANN_VOICE, new Integer(5));
			put(IConstants.ANN_GEND, new Integer(6));
			put(IConstants.ANN_CASE, new Integer(7));
			put(IConstants.ANN_DEG, new Integer(8));
			put(IConstants.ANN_POSS, new Integer(9));
			put(IConstants.ANN_DEF, new Integer(10));
			put(IConstants.ANN_OBJ, new Integer(11));
			put(IConstants.ANN_PFX, new Integer(12));
			put(IConstants.ANN_SORT, new Integer(13));
		}
	};

	/**
	 * Maps the character abbreviations for each postag element to the full
	 * names
	 */
	private static final HashMap<String, HashMap> abbrevMap = new HashMap<String, HashMap>() {
		{
			put(IConstants.ANN_POFS, new HashMap<String, String>() {
				{
					put("n", IConstants.POFS_NOUN);
					put("v", IConstants.POFS_VERB);
					put("t", IConstants.POFS_PARTICIPLE);
					put("a", IConstants.POFS_ADJECTIVE);
					put("d", IConstants.POFS_ADVERB);
					put("c", IConstants.POFS_CONJUNCTION);
					put("l", IConstants.POFS_ARTICLE);
					put("g", IConstants.POFS_PARTICLE);
					put("r", IConstants.POFS_PREPOSITION);
					put("p", IConstants.POFS_PRONOUN);
					put("m", IConstants.POFS_NUMERAL);
					put("i", IConstants.POFS_INTERJECTION);
					put("e", IConstants.POFS_EXCLAMATION);
					put("x", IConstants.POFS_IRREGULAR);
					put("u", IConstants.POFS_PUNCTUATION);
					put("f", IConstants.POFS_FUNCTIONAL);
					put("z", IConstants.POFS_DETERMINER);
				}
			});
			put(IConstants.ANN_PERS, new HashMap<String, String>() {
				{
					put("1", IConstants.PERS_FIRST);
					put("2", IConstants.PERS_SECOND);
					put("3", IConstants.PERS_THIRD);
				}
			});
			put(IConstants.ANN_NUM, new HashMap<String, String>() {
				{
					put("s", IConstants.NUM_SING);
					put("p", IConstants.NUM_PLURAL);
					put("d", IConstants.NUM_DUAL);
				}
			});
			put(IConstants.ANN_TENSE, new HashMap<String, String>() {
				{
					put("p", IConstants.TENSE_PRESENT);
					put("i", IConstants.TENSE_IMPERFECT);
					put("r", IConstants.TENSE_PERFECT);
					put("l", IConstants.TENSE_PLUPERFECT);
					put("t", IConstants.TENSE_FUTUREPERFECT);
					put("f", IConstants.TENSE_FUTURE);
					put("a", IConstants.TENSE_AORIST);
					put("e", IConstants.TENSE_PRETERITE);
					put("c", IConstants.TENSE_CONDITIONAL);
				}
			});
			put(IConstants.ANN_MOOD, new HashMap<String, String>() {
				{
					put("i", IConstants.MOOD_IND);
					put("s", IConstants.MOOD_SUBJ);
					put("o", IConstants.MOOD_OPT);
					put("n", IConstants.MOOD_INF);
					put("m", IConstants.MOOD_IMP);
					put("g", IConstants.MOOD_GERUND);
					put("p", IConstants.MOOD_PART);
				}
			});
			put(IConstants.ANN_VOICE, new HashMap<String, String>() {
				{
					put("m", IConstants.VOICE_MIDDLE);
					put("a", IConstants.VOICE_ACTIVE);
					put("p", IConstants.VOICE_PASSIVE);
					put("d", IConstants.VOICE_DEP);
					put("e", IConstants.VOICE_MEDIOPASS);
				}
			});
			put(IConstants.ANN_GEND, new HashMap<String, String>() {
				{
					put("m", IConstants.GEND_MASC);
					put("f", IConstants.GEND_FEM);
					put("n", IConstants.GEND_NEUT);
					put("c", IConstants.GEND_COM);
				}
			});
			put(IConstants.ANN_CASE, new HashMap<String, String>() {
				{
					put("n", IConstants.CASE_NOM);
					put("g", IConstants.CASE_GEN);
					put("d", IConstants.CASE_DAT);
					put("a", IConstants.CASE_ACC);
					put("b", IConstants.CASE_ABL);
					put("v", IConstants.CASE_VOC);
					put("i", IConstants.CASE_INS);
					put("l", IConstants.CASE_LOC);

				}
			});
			put(IConstants.ANN_DEG, new HashMap<String, String>() {
				{
					put("p", IConstants.DEG_POS);
					put("c", IConstants.DEG_COMP);
					put("s", IConstants.DEG_SUP);
				}
			});
			put(IConstants.ANN_POSS, new HashMap<String, String>() {
				{
					put("a", IConstants.POSS_FP);
					put("b", IConstants.POSS_FS);
					put("c", IConstants.POSS_SD);
					put("d", IConstants.POSS_SFP);
					put("e", IConstants.POSS_SFS);
					put("f", IConstants.POSS_SMP);
					put("g", IConstants.POSS_SMS);
					put("h", IConstants.POSS_TD);
					put("i", IConstants.POSS_TFP);
					put("j", IConstants.POSS_TFS);
					put("k", IConstants.POSS_TMP);
					put("l", IConstants.POSS_TMS);

				}
			});
			put(IConstants.ANN_DEF, new HashMap<String, String>() {
				{
					put("d", IConstants.DEF_DEF);
					put("i", IConstants.DEF_IND);

				}
			});
			put(IConstants.ANN_OBJ, new HashMap<String, String>() {
				{
					put("a", IConstants.OBJ_FP);
					put("b", IConstants.OBJ_FS);
					put("c", IConstants.OBJ_SD);
					put("d", IConstants.OBJ_SFP);
					put("e", IConstants.OBJ_SFS);
					put("f", IConstants.OBJ_SMP);
					put("g", IConstants.OBJ_SMS);
					put("h", IConstants.OBJ_TD);
					put("i", IConstants.OBJ_TFP);
					put("j", IConstants.OBJ_TFS);
					put("k", IConstants.OBJ_TMP);
					put("l", IConstants.OBJ_TMS);

				}
			});
			put(IConstants.ANN_PFX, new HashMap<String, String>() {
				{
					put("c", IConstants.PFX_CONJ);
					put("e", IConstants.PFX_EMP);
					put("i", IConstants.PFX_INT);
					put("n", IConstants.PFX_NEG);
					put("p", IConstants.PFX_PREP);
					put("r", IConstants.PFX_RES);
				}
			});
			put(IConstants.ANN_SORT, new HashMap<String, String>() {
				{
					put("c", IConstants.SORT_CARD);
					put("o", IConstants.SORT_ORD);
				}
			});

		}
	};

	public void setCorpus(SCorpus a_sCorpus) {
		this.addSMetaAnnotationString(a_sCorpus, IConstants.ANN_CORPUS_NAME, a_sCorpus.getName());
		Iterator<SDocument> dIter = this.docList.iterator();
		int num = 0;
		while (dIter.hasNext()) {
			SDocument doc = dIter.next();
			this.addSMetaAnnotationString(a_sCorpus, IConstants.ANN_CORPUS_DOC + (++num), this.getDocName(doc));
			// TODO - eventually add the document titles here too
		}
		Iterator<String> iter = this.allAnnotators.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = this.allAnnotators.get(key);
			this.addSMetaAnnotationString(a_sCorpus, IConstants.ANN_CORPUS_ANNOTATOR + key, value);
		}
		this.initCorpus();
	}

	/**
	 * SAX Handler Methods
	 */
	@Override
	public void startDocument() throws SAXException {
		// System.out.println("Starting document");

	}

	@Override
	public void endDocument() throws SAXException {
		// System.out.println("Ending document");
	}

	@Override
	public void startElement(String a_uri, String a_localName, String a_qName, Attributes a_atts) throws SAXException {

		this.currentText = "";
		if (a_qName.equals("treebank")) {
			// TODO
			// handle attributes:
			// schema
			// lang
			// date
		} else if (a_qName.equals("annotator") && this.currentSentence == null) {
			// this is a new annotator, cache the info
			this.currentAnnotatorInfo = new HashMap<String, String>();
		} else if (a_qName.equals("sentence")) {
			this.startSentence(a_atts);
		} else if (a_qName.equals("word")) {
			this.startWord(a_atts);
		}
	}

	@Override
	public void endElement(String a_uri, String a_localName, String a_qName) throws SAXException {

		if ("date".equals(a_qName)) {
			java.text.DateFormat df = new java.text.SimpleDateFormat(IConstants.DATE_FORMAT, java.util.Locale.US);
			try {
				// TODO add metadata annotation to layer instead of document
				this.addDate(this.getDocument(), df.parse(this.currentText));
			} catch (ParseException a_e) {
				logger.warn("Date " + currentText + " is not in expected format " + IConstants.DATE_FORMAT);
				// if we couldn't parse the date, add it anyway as a string
				this.addDateString(this.getDocument(), this.currentText);
			} catch (Exception e) {
				logger.warn("Unable to add date " + currentText + ":", e);
			}

		} else if (a_qName.equals("annotator") && this.currentSentence == null) {

			// if there is no current sentence, it should be a new document
			// level annotator
			this.addAnnotator(this.currentAnnotatorInfo);
			// TODO - add to layer rather than document?
		} else if (this.currentSentence == null && (a_qName.equals(IConstants.ATT_ANNOTATOR_SHORT) || a_qName.equals(IConstants.ATT_ANNOTATOR_NAME) || a_qName.equals(IConstants.ATT_ANNOTATOR_ADDRESS))) {
			this.currentAnnotatorInfo.put(a_qName, this.currentText);
		} else if (a_qName.equals(IConstants.ATT_ANNOTATOR_PRIMARY) || a_qName.equals(IConstants.ATT_ANNOTATOR_SECONDARY) || (a_qName.equals("annotator") && this.currentSentence != null)) {
			String shortname = IConstants.ANN_ANNOTATOR;
			if (!a_qName.equals("annotator")) {
				shortname = shortname + "_" + a_qName;

			}
			String fullname = IConstants.NS + "::" + shortname;

			SAnnotation ann = this.currentSentence.getAnnotation(fullname);
			if (this.currentSentence != null) {
				if (ann != null) {
					ann.setValue(ann.getValue() + "," + this.currentText);
				} else {
					ann = this.addAnnotationString(this.currentSentence, shortname, this.currentText);
				}
			}
		} else if (a_qName.equals("sentence")) {
			this.endSentence();
		}
	}

	@Override
	public void characters(char a_ch[], int a_start, int a_length) throws SAXException {
		this.currentText = this.currentText + new String(a_ch, a_start, a_length);
	}

	/**********************************************************
	 * SALT Model Accessors
	 * 
	 **********************************************************/

	/**
	 * initialize for a new document
	 */
	private void initDocument() {
		this.currentSentence = null;
		// this.currentSentenceToken = null;
		// this.currentSpan = null;
		this.currentText = null;
		this.currentAnnotatorInfo = null;
		this.sDocument = null;
	}

	/**
	 * Set the special properties
	 */
	public void setProps(Properties a_props) {
		if (a_props != null) {
			citation_base_uri = a_props.getProperty(IConstants.PROP_CITATION_BASE_URI);
		}
	}

	/**
	 * Set the SDocument
	 */
	public void setDocument(SDocument a_sDocument) {
		this.initDocument();
		this.sDocument = a_sDocument;
		this.docList.add(a_sDocument);
		logger.debug("Creating DocumentGraph for " + this.getDocument().getId());
		this.sDocument.setDocumentGraph(SaltFactory.createSDocumentGraph());
		String name = this.getDocName(a_sDocument);
		this.sDocument.getDocumentGraph().setName(name);
		this.sDocument.getDocumentGraph().setId(this.getDocument().getId());

		logger.debug("Adding TextDS");
		// TODO not sure if we want to associate a primary text element or
		// not...
		STextualDS sTextDS = SaltFactory.createSTextualDS();
		this.setText(sTextDS);
		this.sDocument.getDocumentGraph().addNode(sTextDS);
		this.addSMetaAnnotationString(this.getDocument(), IConstants.ANN_DOCUMENT_ID, name);
	}

	/**
	 * Get the SDocument
	 * 
	 * @return
	 */
	public SDocument getDocument() {
		return sDocument;
	}

	/**
	 * Set the STextDS
	 * 
	 * @param a_sTextDS
	 */
	public void setText(STextualDS a_sTextDS) {
		this.sTextDS = a_sTextDS;
	}

	/**
	 * Get the STextDS
	 * 
	 * @return
	 */
	public STextualDS getTextDS() {
		return this.sTextDS;
	}

	/**************************************************************************************
	 * Perseus Treebank to Salt MetaModel Mapping Methods
	 **************************************************************************************/

	/**
	 * Add the Date as an SMetaAnnotation
	 * 
	 * @param a_elem
	 *            the element to add it to
	 * @param a_date
	 *            the date object
	 */
	private void addDate(SAnnotationContainer a_elem, Date a_date) {
		SMetaAnnotation ann = SaltFactory.createSMetaAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setName(IConstants.ANN_DATE);
		ann.setValue(a_date);
		a_elem.addMetaAnnotation(ann);
	}

	/**
	 * Add the Date String as an SMetaAnnotation
	 * 
	 * @param a_elem
	 *            the element to add it to
	 * @param a_date
	 *            the date string
	 */
	private void addDateString(SAnnotationContainer a_elem, String a_date) {
		SMetaAnnotation ann = SaltFactory.createSMetaAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setName(IConstants.ANN_DATE);
		ann.setValue(a_date);
		a_elem.addMetaAnnotation(ann);
	}

	/**
	 * Create an Annotator MetaAnnotation
	 * 
	 * @param a_info
	 *            the accumulated data for the Annotator
	 * @return a String key to access the MetaAnnotation by
	 */
	private void addAnnotator(Map a_info) {
		String key = (String) a_info.get(IConstants.ATT_ANNOTATOR_SHORT);
		String nameStr = (String) a_info.get(IConstants.ATT_ANNOTATOR_NAME);
		String addyStr = (String) a_info.get(IConstants.ATT_ANNOTATOR_ADDRESS);

		HashMap<String, String> ann = new HashMap<String, String>();
		this.allAnnotators.put(key, nameStr + "," + addyStr);
	}

	private String getLongString(String a_att, String a_value) {
		return (String) (this.abbrevMap.get(a_att).get(a_value));
	}

	/**
	 * Maps a Perseus word node to an SToken and adds - relationships between
	 * the word SToken and the current sentence SStructure - syntactic
	 * annotations on the word Dependency relationships between the word and
	 * other words in the sentence are stored for processing at the end of the
	 * sentence
	 * 
	 * @param a_atts
	 *            the attributes of the word node
	 */
	private void startWord(Attributes a_atts) {

		logger.debug("Starting Word");

		SToken sToken = SaltFactory.createSToken();

		// TODO add the word to the layer instead of document?
		this.getDocument().getDocumentGraph().addNode(sToken);

		String postag = a_atts.getValue(IConstants.ATT_POSTAG);
		String lemmaFull = a_atts.getValue(IConstants.ATT_LEMMA);
		String lemma = null;
		String sense = null;
		if (lemmaFull != null) {
			lemma = lemmaFull.replaceAll("[0-9]*$", "");
			sense = lemmaFull.replaceAll("^[^0-9]+", "");
		}
		String form = a_atts.getValue(IConstants.ATT_FORM);
		String relation = a_atts.getValue(IConstants.ATT_RELATION);
		String id = a_atts.getValue(IConstants.ATT_ID);
		String cid = a_atts.getValue(IConstants.ATT_CID);
		String head = a_atts.getValue(IConstants.ATT_HEAD);
		String urn = a_atts.getValue(IConstants.ATT_CITE);

		if (urn != null && !urn.equalsIgnoreCase("")) {
			this.addAnnotationString(sToken, IConstants.ANN_URN, urn);
			if (this.citation_base_uri != null) {
				this.addAnnotationString(sToken, IConstants.ANN_CITE, "<a href='" + this.citation_base_uri + urn + "' target='_blank'>" + IConstants.ANN_CITE_LINK_TEXT + "</a>");
			}
		}
		// TODO create CTS URN to word using id
		this.addAnnotationString(sToken, IConstants.ANN_WORD_ID, cid);

		if (postag != null && !(postag.equalsIgnoreCase(""))) {
			logger.debug("Adding POSTAG annotation");
			// Add as-is as annotation on token
			SAnnotation sAnno = SaltFactory.createSAnnotation();
			sAnno.setNamespace(IConstants.NS);
			sAnno.setName(IConstants.ATT_POSTAG);
			sAnno.setValue(postag);
			sToken.addAnnotation(sAnno);

			// add components of pofstag separate annotations
			SAnnotation pAnno = SaltFactory.createSPOSAnnotation();
			String pos = postag.substring(0, 1);
			if (!pos.equals(IConstants.EMPTY)) {
				logger.debug("Adding POFS annotation");
				pAnno.setValue(this.getLongString(IConstants.ANN_POFS, pos));
				sToken.addAnnotation(pAnno);
			}
			Iterator<String> iter = postagMap.keySet().iterator();
			while (iter.hasNext()) {

				String key = iter.next();

				int index = postagMap.get(key).intValue();
				// this.log(LogService.LOG_DEBUG, "Postag piece: " + key + "=" +
				// index);
				String value = null;
				try {
					value = postag.substring(index, index + 1);
				} catch (java.lang.StringIndexOutOfBoundsException a_e) {
					// no more parts to postag
				}
				if (value != null && !value.equals(IConstants.EMPTY)) {
					logger.debug("Adding annotation: " + key + "=" + value);
					sAnno = SaltFactory.createSAnnotation();
					sAnno.setNamespace(IConstants.NS);
					sAnno.setName(key);
					sAnno.setValue(this.getLongString(key, value));
					sToken.addAnnotation(sAnno);
				}
			}
		}

		if (lemma != null && !lemma.isEmpty()) {

			SAnnotation sAnno = SaltFactory.createSLemmaAnnotation();
			sAnno.setValue(lemma);
			sAnno.setNamespace(IConstants.NS);
			sToken.addAnnotation(sAnno);
			logger.debug("Added Lemma Annotation " + sAnno.getValue_STEXT());

		}

		if (sense != null && !sense.isEmpty()) {
			this.addAnnotationString(sToken, IConstants.ANN_SENSE, sense);
		}

		if (form != null && !form.isEmpty()) {
			logger.debug("Adding Form Annotation");
			SAnnotation sAnno = SaltFactory.createSAnnotation();
			sAnno.setValue(form);
			sAnno.setNamespace(IConstants.NS);
			sAnno.setName(IConstants.ATT_FORM);
			sToken.addAnnotation(sAnno);
		}

		// store the token, head and relation
		this.tokenMap.put(id, sToken);

		if (!(head.equals("0"))) {
			this.saveDependency(head, id, relation);
		} else {
			logger.debug("Linking root word to sentence " + this.currentSentence.getId());
			// add dominance relationship between the word and the sentence
			SDominanceRelation tDomRel = SaltFactory.createSDominanceRelation();
			tDomRel.setSource(this.currentSentence);
			tDomRel.setTarget(sToken);
			SAnnotation sAnno = SaltFactory.createSAnnotation();
			sAnno.setNamespace(IConstants.NS);
			sAnno.setName(IConstants.ANN_RELATION);
			sAnno.setValue(relation);
			tDomRel.addAnnotation(sAnno);
			this.getDocument().getDocumentGraph().addRelation(tDomRel);
			logger.debug("Linked root word to sentence " + this.currentSentence.getId());
		}
		// create an empty text, if there is none
		if (this.getTextDS().getText() == null)
			this.getTextDS().setText("");

		// if text of token isn't empty put a separator to its end

		this.getTextDS().setText(this.getTextDS().getText() + this.DEFAULT_SEPARATOR);

		Integer startPos = 0;
		Integer endPos = 0;

		// set startpos to current text length
		startPos = this.getTextDS().getText().length();
		this.getTextDS().setText(this.getTextDS().getText() + form);
		endPos = this.getTextDS().getText().length();

		// create STextualRelation
		STextualRelation sTextRel = SaltFactory.createSTextualRelation();
		sTextRel.setTarget(this.getTextDS());
		sTextRel.setSource(sToken);
		sTextRel.setStart(startPos);
		sTextRel.setEnd(endPos);
		this.getDocument().getDocumentGraph().addRelation(sTextRel);

	}

	/**
	 * Maps all sentences to SSTructures.
	 * 
	 * @param nts
	 */
	private void startSentence(Attributes a_atts) {
		logger.debug("Adding Sentence " + a_atts.getValue("id"));
		SStructure sStructure = SaltFactory.createSStructure();
		this.getDocument().getDocumentGraph().addNode(sStructure);
		this.currentSentence = sStructure;

		SAnnotation sAnno = SaltFactory.createSCatAnnotation();
		sAnno.setNamespace(IConstants.NS);
		sAnno.setValue(IConstants.ANN_SENTENCE);
		sStructure.addAnnotation(sAnno);

		String docId = a_atts.getValue(IConstants.ATT_DOCID);
		String subDoc = a_atts.getValue(IConstants.ATT_SUBDOC);
		String span = a_atts.getValue(IConstants.ATT_SPAN);
		String id = a_atts.getValue(IConstants.ATT_ID);

		ArrayList<String> chunkIds = new ArrayList<String>();
		String[] subdocs = subDoc.split(" ");
		for (int i = 0; i < subdocs.length; i++) {
			String citeDoc = subdocs[i];
			if (citeDoc.startsWith("urn:cts:")) {
				// if we have a CTS urn as the subdoc, use it as-is
				chunkIds.add(citeDoc);
			} else {
				// otherwise preface it with the docId
				chunkIds.add(docId + ":" + citeDoc);
			}
		}
		this.addAnnotationString(sStructure, IConstants.ANN_SENTENCE_ID, id);
		this.addAnnotationString(sStructure, IConstants.ANN_SUBDOC, StringUtils.join(chunkIds.toArray(), ","));
		if (span != null) {
			this.addAnnotationString(sStructure, IConstants.ANN_SPAN, span.replaceAll("\\\\", java.util.regex.Matcher.quoteReplacement("\\\\")));
		}

		// TODO add id as meta annotation
		tokenMap = new HashMap<String, SToken>();
		tokenRelMap = new HashMap<String, ArrayList<HashMap<String, String>>>();
	}

	/**
	 * Add dependency relationships between the word tokens in the Sentence
	 */
	private void endSentence() {
		Iterator<String> iter = tokenMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			this.setDependency(key);
		}
	}

	private void saveDependency(String a_source, String a_target, String a_rel) {
		HashMap<String, String> relMap = new HashMap<String, String>();
		relMap.put(a_target, a_rel);
		if (!this.tokenRelMap.containsKey(a_source)) {
			this.tokenRelMap.put(a_source, new ArrayList<HashMap<String, String>>());
		}
		this.tokenRelMap.get(a_source).add(relMap);
	}

	private void setDependency(String a_source) {
		SToken parent = this.tokenMap.get(a_source);
		logger.debug("Adding Token Dependency:" + a_source);
		ArrayList<HashMap<String, String>> rels = this.tokenRelMap.get(a_source);
		if (rels != null) {
			Iterator<HashMap<String, String>> iter = rels.iterator();
			while (iter.hasNext()) {
				HashMap<String, String> relMap = iter.next();
				Iterator<String> kiter = relMap.keySet().iterator();
				while (kiter.hasNext()) {
					String key = kiter.next();
					SPointingRelation sRel = SaltFactory.createSPointingRelation();
					sRel.setType(IConstants.ANN_REL_TYPE_PARENT);
					sRel.setSource(parent);
					this.getDocument().getDocumentGraph().addRelation(sRel);
					logger.debug("Adding Dependency Relation: " + (String) relMap.get(key) + " " + a_source + " to " + key + " node " + this.tokenMap.get(key));
					sRel.setTarget((SToken) this.tokenMap.get(key));
					SAnnotation sAnno = SaltFactory.createSAnnotation();
					sAnno.setNamespace(IConstants.NS);
					sAnno.setName(IConstants.ANN_RELATION);
					sAnno.setValue((String) relMap.get(key));
					sRel.addAnnotation(sAnno);
				}
			}
		}
	}

	public static void main(String[] a_args) {
		String documentPath = a_args[0];
		Perseus2SaltMapper mapper = new Perseus2SaltMapper();
		SDocument sdoc = SaltFactory.createSDocument();
		mapper.setDocument(sdoc);
		File resourceFolder = new File("./src/main/resources");
		mapper.setResourcesURI(URI.createFileURI(resourceFolder.getAbsolutePath()));
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(documentPath, mapper);
		} catch (Exception a_e) {
			throw new PepperModuleException("Unable to parse " + documentPath + ":", a_e);
		}
	}

	private SMetaAnnotation addSMetaAnnotationString(SAnnotationContainer a_elem, String a_name, String a_value) {
		SMetaAnnotation ann = SaltFactory.createSMetaAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setName(a_name);
		ann.setValue(a_value);
		a_elem.addMetaAnnotation(ann);
		return ann;
	}

	private SAnnotation addAnnotationString(SAnnotationContainer a_elem, String a_name, String a_value) {
		SAnnotation ann = SaltFactory.createSAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setName(a_name);
		ann.setValue(a_value);
		a_elem.addAnnotation(ann);
		return ann;
	}

	private SProcessingAnnotation addSProcessingAnnotationString(SAnnotationContainer a_elem, String a_name, String a_value) {
		SProcessingAnnotation ann = SaltFactory.createSProcessingAnnotation();
		ann.setNamespace(IConstants.NS);
		ann.setName(a_name);
		ann.setValue(a_value);
		a_elem.addProcessingAnnotation(ann);
		return ann;
	}

	private String getDocName(SDocument a_doc) {
		String name = "";
		URI uri = a_doc.getPath();
		int segs = uri.segmentCount();
		if (segs > 1) // not a top level corpus
		{
			for (int i = 1; i < segs; i++) {
				if (!name.isEmpty()) {
					name = name + ".";
				}
				name = name + uri.segment(i);
			}
		} else {
			name = uri.segment(0);
		}
		return name;
	}
}
