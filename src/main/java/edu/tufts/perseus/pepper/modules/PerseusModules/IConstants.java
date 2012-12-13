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

public interface IConstants {
	
	public static final String EMPTY = "-";
	public static final String NS = "perseus";
	public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";
	
	// TODO these should really all use ISOCat uris and be translated in the interface
	// via properties set in a string bundle
	public static final String ANN_DATE = "date";
	public static final String ATT_SPAN = "span";
	public static final String ATT_POSTAG = "postag";
	public static final String ATT_PRIMARY = "primary";
	public static final String ATT_SECONDARY = "secondary";
	public static final String ATT_LEMMA = "lemma";	
	public static final String ATT_FORM = "form";
	public static final String ATT_CITE = "cite";
	
	
	public static final String ATT_RELATION = "relation";
	public static final String ATT_ID = "id";
	public static final String ATT_CID = "cid";
	public static final String ATT_HEAD = "head";
	public static final String ATT_DOCID = "document_id";
	public static final String ATT_SUBDOC = "subdoc";
	
	public static final String ANN_SUBDOC = "subdoc";
	public static final String ANN_SPAN = "span";
	
	public static final String ANN_DOCUMENT_NAME = "document_name";	
	public static final String ANN_SENTENCE_ID = "sentence_id";
	public static final String ANN_SENTENCESPAN_ID = "span_id";
	public static final String ANN_CORPUS_NAME = "CORPUS";
	public static final String ANN_DOCUMENT_ID = "DOCUMENT_ID";
	public static final String ANN_CORPUS_DOC = "CORPUS_DOC";
	public static final String ANN_WORD_ID = "word_id";
	public static final String ANN_CITE = "cite";

	
	public static final String ANN_REL_TYPE_CHILD = "child";
	public static final String ANN_REL_TYPE_PARENT = "parent";
	public static final String ANN_LEMMA_BETA = "lemma-beta";
	public static final String ANN_SENSE = "sense";
	public static final String ANN_FORM_BETA = "form-beta";
	public static final String ANN_ROOT = "ROOT";
	public static final String ANN_CORPUS_ANNOTATOR = "CORPUS_annotator_";
	
	public static final String ATT_ANNOTATOR_PRIMARY = "primary";
	public static final String ATT_ANNOTATOR_SECONDARY = "secondary";
	
	public static final String ANN_ANNOTATOR = "annotator";
	public static final String ANN_ANNOTATOR_PRIMARY = "_primary";
	public static final String ANN_ANNOTATOR_SECONDARY = "_secondary";
	
	public static final String ATT_ANNOTATOR_SHORT ="short";	
	public static final String ATT_ANNOTATOR_NAME ="name";
	public static final String ATT_ANNOTATOR_ADDRESS ="address";
	public static final String ANN_ANNOTATOR_NAME ="name";
	public static final String ANN_ANNOTATOR_ADDRESS ="address";
	public static final String ANN_ANNOTATOR_SHORT ="short";	
		
	public static final String ANN_RELATION = "relation";
	public static final String ANN_CAT = "cat";
	public static final String ANN_SENTENCE = "S";
	public static final String ANN_SENTENCE_TOKEN = "T";	
	public static final String ANN_WORD = "W";
	public static final String ANN_WORD_TOKEN = "T";
	public static final String ANN_ANNOT_TOKEN = "A";
	public static final String ANN_RELATION_SPAN = "SPAN";
	public static final String ANN_POFS = "part-of-speech";
	public static final String ANN_PERS = "person";
	public static final String ANN_VOICE = "voice";
	public static final String ANN_TENSE = "tense";
	public static final String ANN_MOOD= "mood";
	public static final String ANN_GEND = "gender";
	public static final String ANN_NUM = "number";
	public static final String ANN_CASE = "case";
	public static final String ANN_DEG = "degree";
	public static final String ANN_POSS = "possessive";
	public static final String ANN_DEF = "definite";
	public static final String ANN_OBJ = "object";
	public static final String ANN_PFX = "prefix";
	public static final String ANN_SORT = "sort";
	public static final String POFS_NOUN = "noun";
	public static final String POFS_VERB = "verb";
	public static final String POFS_PARTICIPLE = "participle";
	public static final String POFS_ADJECTIVE = "adjective";
	public static final String POFS_ADVERB = "adverb";
	public static final String POFS_CONJUNCTION = "conjunction";
	public static final String POFS_ARTICLE = "article";
	public static final String POFS_PARTICLE = "particle";
	public static final String POFS_PREPOSITION = "preposition";
	public static final String POFS_PRONOUN = "pronoun";
	public static final String POFS_NUMERAL = "numeral";
	public static final String POFS_INTERJECTION = "interjection";
	public static final String POFS_EXCLAMATION = "exclamation";
	public static final String POFS_IRREGULAR = "irregular";
	public static final String POFS_PUNCTUATION = "punctuation";
	public static final String POFS_FUNCTIONAL = "functional";
	public static final String POFS_DETERMINER = "determiner";
	public static final String PERS_FIRST = "first";
	public static final String PERS_SECOND = "second";
	public static final String PERS_THIRD = "third";
	public static final String NUM_SING = "singular";
	public static final String NUM_PLURAL = "plural";
	public static final String NUM_DUAL = "dual";
	public static final String TENSE_PRESENT = "present";
	public static final String TENSE_IMPERFECT = "imperfect";
	public static final String TENSE_PERFECT = "perfect";
	public static final String TENSE_PLUPERFECT = "pluperfect";
	public static final String TENSE_FUTUREPERFECT = "future perfect";
	public static final String TENSE_FUTURE = "future";
	public static final String TENSE_AORIST = "aorist";
	public static final String TENSE_PRETERITE = "preterite";
	public static final String TENSE_CONDITIONAL = "conditional";
	public static final String MOOD_IND = "indicative";
	public static final String MOOD_SUBJ = "subjunctive";
	public static final String MOOD_OPT = "optative";
	public static final String MOOD_INF = "infinitive";
	public static final String MOOD_IMP = "imperative";
	public static final String MOOD_GERUND = "gerundive";
	public static final String MOOD_PART = "participial";
	public static final String VOICE_ACTIVE = "active";
	public static final String VOICE_MIDDLE = "middle";
	public static final String VOICE_PASSIVE = "passive";
	public static final String VOICE_DEP = "deponent";
	public static final String VOICE_MEDIOPASS = "mediopassive";
	public static final String GEND_MASC = "masculine";
	public static final String GEND_FEM = "feminine";
	public static final String GEND_NEUT = "neuter";
	public static final String GEND_COM = "common";
	public static final String CASE_GEN = "genitive";
	public static final String CASE_NOM = "nominative";
	public static final String CASE_DAT = "dative";
	public static final String CASE_ACC = "accusative";
	public static final String CASE_ABL = "ablative";
	public static final String CASE_VOC = "vocative";
	public static final String CASE_INS = "instrumental";
	public static final String CASE_LOC = "locative";
	public static final String DEG_POS = "positive";
	public static final String DEG_COMP = "comparative";
	public static final String DEG_SUP = "superlative";
	public static final String POSS_FP = "possessive first plural";
	public static final String POSS_FS = "possessive first singular";
	public static final String POSS_SD = "possessive second dual";
	public static final String POSS_SFP = "possessive second feminine plural";
	public static final String POSS_SFS = "possessive second feminine singular";
	public static final String POSS_SMP = "possessive second masculine plural";
	public static final String POSS_SMS = "possessive second masculine singular";
	public static final String POSS_TD = "possessive third dual";
	public static final String POSS_TFP = "possessive third feminine plural";
	public static final String POSS_TFS = "possessive third feminine singular";
	public static final String POSS_TMP = "possessive third masculine plural";
	public static final String POSS_TMS = "possessive third masculine singular";
	public static final String DEF_DEF = "definite";
	public static final String DEF_IND = "indefinite";
	public static final String OBJ_FP = "first plural";
	public static final String OBJ_FS = "first singular";
	public static final String OBJ_SD = "second dual";
	public static final String OBJ_SFP = "second feminine plural";
	public static final String OBJ_SFS = "second feminine singular";
	public static final String OBJ_SMP = "second masculine plural";
	public static final String OBJ_SMS = "second masculine singular";
	public static final String OBJ_TD = "third dual";
	public static final String OBJ_TFP = "third feminine plural";
	public static final String OBJ_TFS = "third feminine singular";
	public static final String OBJ_TMP = "third masculine plural";
	public static final String OBJ_TMS = "third masculine singular";
	public static final String PFX_CONJ = "conj+";
	public static final String PFX_EMP = "emphatic+";
	public static final String PFX_INT = "interrogative+";
	public static final String PFX_NEG = "neg+";
	public static final String PFX_PREP = "prep+";
	public static final String PFX_RES = "resultative+";
	public static final String SORT_CARD = "cardinal";
	public static final String SORT_ORD = "ordinal";

	// Dummy for transform
	 // an identity copy stylesheet
    public static final String DUMMY = "<root></root>";
  


	
	
	
	
	
	
}
