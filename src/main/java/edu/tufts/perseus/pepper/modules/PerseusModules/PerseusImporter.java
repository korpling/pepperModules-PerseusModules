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

import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;


/**
 * This {@link PepperImporter} imports data coming from the Perseus format into Salt.
 * @author Bridget Almas
 * @version 1.0
 *
 */
@Component(name="AldtImporterComponent", factory="PepperImporterComponentFactory")
public class PerseusImporter extends PepperImporterImpl implements PepperImporter
{	
	private Perseus2SaltMapper mapper = null;
	private Properties props;
	
	public PerseusImporter()
	{
		super();
		//set name of module
		this.setName("AldtImporter");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-PerseusModules"));
		setDesc("This importer transforms data in aldt format used in the Perseus project to a Salt model. ");
		//set list of formats supported by this module
		addSupportedFormat("aldt", "1.0", null);
		addSupportedFormat("aldt", "1.5", null);
		getDocumentEndings().add(PepperImporter.ENDING_XML);
		this.mapper= new Perseus2SaltMapper();
	}
	
	/**
	 * This method is called by method start() of superclass PepperImporter, 
	 * if the method was not overridden by the current class. 
	 * If this is not the case, this method will be called for 
	 * every document which has to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(Identifier sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getIdentifiableElement()!= null) &&
				((sElementId.getIdentifiableElement() instanceof SDocument) ||
				((sElementId.getIdentifiableElement() instanceof SCorpus))))
		{
			
			if (sElementId.getIdentifiableElement() instanceof SCorpus)
			{

				SCorpus sCorpus= (SCorpus) sElementId.getIdentifiableElement();
				{					
					//Corpus corpus = new Corpus(documentPath.toFileString());											
					mapper.setCorpus(sCorpus);											
				}
			}
			//if elementId belongs to SDocument
			else if((sElementId.getIdentifiableElement() instanceof SDocument))
			{			
				URI documentPath= getIdentifier2ResourceTable().get(sElementId);
				if (documentPath!= null)
				{
					SDocument sDoc= (SDocument) sElementId.getIdentifiableElement();
					{	
						this.mapper.setDocument(sDoc);
						this.mapper.setResourcesURI(this.getResources());
						this.mapper.setProps(this.props);
			            SAXParserFactory factory = SAXParserFactory.newInstance();			            
			            try {
			            	SAXParser parser = factory.newSAXParser();
				            parser.parse(documentPath.toFileString(),mapper);
			            } catch (Exception a_e)
			            {
			            	throw new PepperModuleException(this, "Unable to parse " + documentPath.toFileString() + ":",a_e);
			            }			            			             
					}						
				}
			}
		}					
	}
}
