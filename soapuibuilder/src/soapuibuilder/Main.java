/**
 * 
 */
package soapuibuilder;


import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlTestCasePro;
import com.eviware.soapui.impl.wsdl.WsdlTestSuitePro;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDataSourceLoopTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDataSourceTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.datasource.DirectoryDataSource;
import com.eviware.soapui.impl.wsdl.teststeps.registry.DataSourceLoopStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.DataSourceStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import soapuibuilder.models.OperationTestStep;
import soapuibuilder.models.SoapuibuilderConfig;
import soapuibuilder.models.TestStepType;

/**
 * @author Hamza BOURKIA
 *
 */
public class Main {
	
	
	public static final String CONFIG_FILE_PATH="soapuibuilder.config.json";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		SoapuibuilderConfig config = new SoapuibuilderConfig();
		
		
		try {

			
			com.google.gson.stream.JsonReader jReader = new com.google.gson.stream.JsonReader(new FileReader(CONFIG_FILE_PATH));
			config = gson.fromJson(jReader, SoapuibuilderConfig.class);
			
			WsdlProjectPro project = generateSoapuiProject(config);
			
			Paths.get(config.outputDirectory).toFile().mkdirs();
			
			Path fileOutputPath = Paths.get(config.outputDirectory, config.outputFilename);
			
			System.out.println("Saving file to : " + fileOutputPath.toString());
			
			project.saveAs(fileOutputPath.toString());
			
			System.exit(0);
			
			
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SoapUIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

		
		
		

	}
	
	
	private static WsdlProjectPro generateSoapuiProject(SoapuibuilderConfig config) throws SoapUIException {
		
		WsdlProjectPro project = null;
		if(config != null) {
			project = new WsdlProjectPro();
			project.setName(config.projectName);
			WsdlInterface iface = WsdlInterfaceFactory.importWsdl(project, config.wsdlUrl, true)[0];
			
			for(String testSuite : config.testsuites) {
				WsdlTestSuitePro tsuite = project.addNewTestSuite(testSuite);
				
				
				List<WsdlOperation> operations = iface.getAllOperations();
				
				for (WsdlOperation op : operations) {
					
					Path testDirectoryPath = Paths.get(config.outputDirectory, "/../Donnees/DEV/" + tsuite.getName() + "/" + op.getName() );
					System.out.println("Creating the following directory : " + testDirectoryPath.toString());
					boolean success = testDirectoryPath.toFile().mkdirs();
					if(!success) {
						System.err.println("Failed to Create input folder for TestSuite:'" + tsuite.getName() + "' and Operation:'" + op.getName() +"' on the path :\r\n" + testDirectoryPath.toString());
					}
					
					WsdlTestCasePro tcase = (WsdlTestCasePro) tsuite.addNewTestCase(op.getName());

					generateTestSteps(op, tcase, config);
					
					
					
				}
				
				
			}
			
			
			
			
		}
		
		return project;
	}
	
	private static WsdlTestCasePro generateTestSteps(WsdlOperation op, WsdlTestCasePro tcase, SoapuibuilderConfig config) {
		
		for(OperationTestStep tstepc : config.operationTestSteps) {
			
				TestStepType value = null;
				try {
					value = TestStepType.valueOf(tstepc.type.toUpperCase());
					switch (value) {
					case GROOVY:
						WsdlTestStep ts = tcase.addTestStep("groovy", tstepc.name);
						generateInnerPropertiesForTestStep(ts, tstepc);
						break;
					case DATASOURCE:
						TestStepConfig tsc = new DataSourceStepFactory().createNewTestStep(tcase, tstepc.name);
						WsdlDataSourceTestStep tsds = new WsdlDataSourceTestStep(tcase, tsc, false);
						DirectoryDataSource dds = (DirectoryDataSource) tsds.setDataSource("Directory");
						dds.setDirectory(tstepc.directory);
						dds.setFilter(tstepc.filter);
						dds.setEncoding(tstepc.encoding);
						dds.getDataSourceContainer().addProperty("FileContent");
						dds.getDataSourceContainer().addProperty("Filename");
						tcase.insertTestStep(tsds.getConfig(), tcase.getTestStepCount());
						ts = tcase.getTestStepAt(tcase.getTestStepCount() - 1);
						generateInnerPropertiesForTestStep(ts, tstepc);
						break;
					case DATASOURCELOOP : 
						tsc = new DataSourceLoopStepFactory().createNewTestStep(tcase, tstepc.name);
						WsdlDataSourceLoopTestStep dslts = new WsdlDataSourceLoopTestStep(tcase, tsc, false);
						dslts.setTargetStep(tstepc.targetStep);
						dslts.setDataSourceStep(tstepc.dataSourceStep);
						tcase.insertTestStep(dslts.getConfig(), tcase.getTestStepCount());
						ts = tcase.getTestStepAt(tcase.getTestStepCount() - 1);
						generateInnerPropertiesForTestStep(ts, tstepc);
						break;
					case REQUEST :
						tsc = new WsdlTestRequestStepFactory().createNewTestStep(op, new StringToStringMap());
						tsc.setName(tstepc.name);
						tcase.insertTestStep(tsc, tcase.getTestStepCount());
						WsdlTestRequestStep tsr = (WsdlTestRequestStep) tcase.getTestStepAt(tcase.getTestStepCount() - 1);
						tsr.addAssertion("SOAP Response").setName("SOAP Response");
						tsr.addAssertion("Schema Compliance").setName("Schema Compliance");
						if(tstepc.name.contains("Succes")) {
							//In case of success, we add the success assertions

							WsdlRequestConfig aconf = tsr.getTestRequest().getConfig();
							TestAssertionConfig taconf = aconf.addNewAssertion();
							taconf.setType("SOAP Fault Assertion");
							taconf.setName("Not SOAP Fault");
							tsr.getTestRequest().setConfig(aconf);
							
						}else {
							//In case of error, we add the error assertions
							WsdlRequestConfig aconf = tsr.getTestRequest().getConfig();
							TestAssertionConfig taconf = aconf.addNewAssertion();
							taconf.setType("Not SOAP Fault Assertion");
							taconf.setName("SOAP Fault");
							tsr.getTestRequest().setConfig(aconf);
						}
						TestAssertion ta = tsr.addAssertion("Valid HTTP Status Codes");
						ta.setName("Valid HTTP Status Codes");
						((ValidHttpStatusCodesAssertion) ta).setCodes("${#Project#Statut}");
						ts = tsr;
						generateInnerPropertiesForTestStep(ts, tstepc);
						break;

					}
				}catch(IllegalArgumentException e) {
					System.err.println("Unsupported TestStepType " + tstepc.type);
				}
			
			
			
		}
		
		
		
		return tcase;
	}
	
	private static WsdlTestStep generateInnerPropertiesForTestStep(WsdlTestStep ts, OperationTestStep tstepc) {
		
		
		Enumeration<Object> enumerator = tstepc.properties.keys();
		while(enumerator.hasMoreElements()) {
			String key = (String) enumerator.nextElement();
			ts.setPropertyValue(key, tstepc.properties.getProperty(key));
			
		}
		
		return ts;
	}

}
