/**
 * 
 */
package soapuibuilder.models;


import java.util.Properties;

/**
 * @author DQG1515
 *
 */
public class OperationTestStep {
	
	
	
	/**
	 * 
	 */
	public OperationTestStep() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param name
	 * @param type
	 * @param properties
	 * @param directory
	 * @param filter
	 * @param encoding
	 * @param targetStep
	 * @param dataSourceStep
	 */
	public OperationTestStep(String name, String type, Properties properties, String directory, String filter,
			String encoding, String targetStep, String dataSourceStep) {
		super();
		this.name = name;
		this.type = type;
		this.properties = properties;
		this.directory = directory;
		this.filter = filter;
		this.encoding = encoding;
		this.targetStep = targetStep;
		this.dataSourceStep = dataSourceStep;
	}
	public String name;
	public String type;
	public Properties properties = new Properties();
	public String directory;
	public String filter;
	public String encoding;
	public String targetStep;
	public String dataSourceStep;
	
	
}
