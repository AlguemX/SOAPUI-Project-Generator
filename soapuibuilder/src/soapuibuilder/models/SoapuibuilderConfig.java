/**
 * 
 */
package soapuibuilder.models;

import java.util.ArrayList;

/**
 * @author DQG1515
 *
 */
public class SoapuibuilderConfig {
	
	
	
	/**
	 * 
	 */
	public SoapuibuilderConfig() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param projectName
	 * @param wsdlUrl
	 * @param outputDirectory
	 * @param outputFilename
	 * @param testsuites
	 * @param operationTestSteps
	 */
	public SoapuibuilderConfig(String projectName, String wsdlUrl, String outputDirectory, String outputFilename,
			ArrayList<String> testsuites, ArrayList<OperationTestStep> operationTestSteps) {
		super();
		this.projectName = projectName;
		this.wsdlUrl = wsdlUrl;
		this.outputDirectory = outputDirectory;
		this.outputFilename = outputFilename;
		this.testsuites = testsuites;
		this.operationTestSteps = operationTestSteps;
	}
	public String projectName;
	public String wsdlUrl;
	public String outputDirectory;
	public String outputFilename;
	public ArrayList<String> testsuites = new ArrayList<String>() ;
	public ArrayList<OperationTestStep> operationTestSteps = new ArrayList<OperationTestStep>();


}
