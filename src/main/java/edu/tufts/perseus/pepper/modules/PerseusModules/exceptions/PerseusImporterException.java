package edu.tufts.perseus.pepper.modules.PerseusModules.exceptions;

import de.hub.corpling.pepper.pepperExceptions.PepperModuleException;

public class PerseusImporterException extends PepperModuleException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PerseusImporterException()
	{ super(); }
	
    public PerseusImporterException(String s)
    { super(s); }
    
	public PerseusImporterException(String s, Throwable ex)
	{
		super(s + " Nested Exception is: "+ ex.getMessage()+ ".", ex); 
	}
}
