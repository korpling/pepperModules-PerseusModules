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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import edu.tufts.perseus.pepper.modules.PerseusModules.exceptions.PerseusImporterException;


/**
 * This {@link PepperImporter} imports data coming from the Perseus format into Salt.
 * @author Bridget Almas
 * @version 1.0
 *
 */
@Component(name="EXMARaLDAImporterComponent", factory="PepperImporterComponentFactory")
@Service(value=PepperImporter.class)
public class PerseusImporter extends PepperImporterImpl implements PepperImporter
{
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> documentResourceTable= null;
	private Perseus2SaltMapper mapper = null;
	
	public PerseusImporter()
	{
		super();
		
		{//setting name of module
			//TODO /1/: change the name of the module, for example use the format name and the ending Importer (FORMATImporter)
			this.name= "PerseusImporter";
		}//setting name of module
		
		{//for testing the symbolic name has to be set without osgi
			if (	(this.getSymbolicName()==  null) ||
					(this.getSymbolicName().isEmpty()))
				//TODO /2/: change the symbolic name to your symbolic name as in OSGI-Meta-Inf 
				this.setSymbolicName("edu.tufts.perseus.pepper.modules.PerseusModules");
		}//for testing the symbolic name has to be set without osgi
		
		{//set list of formats supported by this module
			this.supportedFormats= new BasicEList<FormatDefinition>();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			//TODO /3/:change "sample" with format name
			formatDef.setFormatName("Perseus");
			//TODO /4/:change 1.0 with format version to support
			formatDef.setFormatVersion("1.0");
			this.supportedFormats.add(formatDef);
		}
		
		{//just for logging: to say, that the current module has been loaded
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is created...");
		}//just for logging: to say, that the current module has been loaded
		this.mapper= new Perseus2SaltMapper();		
	}
	
	
	/**
	 * This method is called by Pepper at the start of conversion process. 
	 * It shall create the structure the corpus to import. 
	 * That means creating all necessary SCorpus, 
	 * SDocument and all Relation-objects between them. The path to 
	 * the corpus to import is given by this.getCorpusDefinition().getCorpusPath().
	 * @param an empty graph given by Pepper, which shall contains the corpus structure
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph corpusGraph)
			throws PepperModuleException 
	{
		//this.setSCorpusGraph(corpusGraph);
		if (this.getSCorpusGraph()== null)
			throw new PerseusImporterException(
				this.name + 
				": Cannot start with importing corpus, " + 
				"because salt project is not set.");
		
		if (this.getCorpusDefinition()== null)
			throw new PerseusImporterException(
				this.name + 
				": Cannot start with importing corpus, " + 
				"because no corpus definition to import is given.");
		
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new PerseusImporterException(
				this.name + 
				": Cannot start with importing corpus, " + 
				"because the path of given corpus definition is null.");		
		if (this.getCorpusDefinition().getCorpusPath().isFile())
		{
			this.documentResourceTable = new Hashtable<SElementId, URI>();

			// clean uri in corpus path (if it is a folder and ends with /, 
			// / has to be removed)
			if ( (this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("/")) || 
				 (this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("\\")))
			{
				this.getCorpusDefinition().setCorpusPath(this.getCorpusDefinition().getCorpusPath().trimSegments(1));
			}
			try {
				EList<String> endings= new BasicEList<String>();
				endings.add("xml");
				this.documentResourceTable = 
					this.createCorpusStructure(
							this.getCorpusDefinition().getCorpusPath(), 
							null, 
							endings);
			} catch (IOException e) {
				throw new PerseusImporterException(
						this.name + 
						": Cannot start with importing corpus, "  + 
						" because some exception occurs: ",e);
			}
		}	 	
	}
	
	/**
	 * If this method is not really implemented, it will call the Method start(sElementId) for every document 
	 * and corpus, which shall be processed. If it is not really implemented, the method-call will be serial and
	 * and not parallel. To implement a parallelization override this method and take care, that your code is
	 * thread-safe. 
	 * For getting an impression how to implement this method, here is a snipplet of super class 
	 * PepperImporter of this method:
	 * <br/>
	 * boolean isStart= true;
	 * SElementId sElementId= null;
	 * while ((isStart) || (sElementId!= null))
	 * {	
	 *  isStart= false;
	 *		sElementId= this.getPepperModuleController().get();
	 *		if (sElementId== null)
	 *			break;
	 *		
	 *		//call for using push-method
	 *		this.start(sElementId);
	 *		
	 *		if (this.returningMode== RETURNING_MODE.PUT)
	 *		{	
	 *			this.getPepperModuleController().put(sElementId);
	 *		}
	 *		else if (this.returningMode== RETURNING_MODE.FINISH)
	 *		{	
	 *			this.getPepperModuleController().finish(sElementId);
	 *		}
	 *		else 
	 *			throw new PepperModuleException("An error occurs in this module (name: "+this.getName()+"). The returningMode isn't correctly set (it's "+this.getReturningMode()+"). Please contact module supplier.");
	 *		this.end();
	 *	}
	 * After all documents were processed this method of super class will call the method end().
	 */
	@Override
	public void start() throws PepperModuleException
	{
		//TODO /5/: delete this, if you want to parallelize processing 
		super.start();
	}
	
	/**
	 * This method is called by method start() of superclass PepperImporter, 
	 * if the method was not overridden by the current class. 
	 * If this is not the case, this method will be called for 
	 * every document which has to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument) ||
				((sElementId.getSIdentifiableElement() instanceof SCorpus))))
		{
			
			if (sElementId.getSIdentifiableElement() instanceof SCorpus)
			{

				SCorpus sCorpus= (SCorpus) sElementId.getSIdentifiableElement();
				{					
					//Corpus corpus = new Corpus(documentPath.toFileString());											
					mapper.setCorpus(sCorpus);											
				}
			}
			//if elementId belongs to SDocument
			else if((sElementId.getSIdentifiableElement() instanceof SDocument))
			{					
				URI documentPath= this.documentResourceTable.get(sElementId);
				if (documentPath!= null)
				{
					SDocument sDoc= (SDocument) sElementId.getSIdentifiableElement();
					{	
						mapper.setLogService(this.getLogService());
						this.mapper.setDocument(sDoc);									
			            SAXParserFactory factory = SAXParserFactory.newInstance();			            
			            try {
			            	SAXParser parser = factory.newSAXParser();
				            parser.parse(documentPath.toFileString(),mapper);	
			            } catch (Exception a_e)
			            {
			            	throw new PerseusImporterException(
			            			"Unable to parse " + documentPath.toFileString() + ":",a_e);
			            }			            			             
					}						
				}
			}
		}					
	}
	
	/**
	 * This method is called by method start() of super class PepperModule. If you do not implement
	 * this method, it will call start(sElementId), for all super corpora in current SaltProject. The
	 * sElementId refers to one of the super corpora. 
	 */
	@Override
	public void end() throws PepperModuleException
	{
		//TODO /8/: implement this method when necessary 
		super.end();
	}
	
//================================ start: methods used by OSGi
	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets activated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void activate(ComponentContext componentContext) 
	{
		
		//this.setSymbolicName(componentContext.getBundleContext().getBundle().getSymbolicName());
		{//just for logging: to say, that the current module has been activated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is activated...");
		}//just for logging: to say, that the current module has been activated
		
	}

	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets deactivated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void deactivate(ComponentContext componentContext) 
	{
		{//just for logging: to say, that the current module has been deactivated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is deactivated...");
		}	
	}
//================================ start: methods used by OSGi
}
