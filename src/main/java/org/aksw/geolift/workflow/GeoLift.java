/**
 * 
 */
package org.aksw.geolift.workflow;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.aksw.geolift.io.Reader;
import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public class GeoLift {
	private static final Logger logger = Logger.getLogger(GeoLift.class.getName());
	private static final String helpMessage = 
			"parameters:\n" +
				"\t-i --input: input file/URI" + "\n" +
				"\t-o --output: output file/URI" + "\n" +
				"\t-c --Config: config file to read the parameters for each module from" + "\n" +
				"Config file format:" + "\n"+
				"\t moduleNo moduleName moduleParameterName moduleParameterValue" + "\n"+
				"Config File Example:\n" +
				"\t1 nlp useFoxLight true" + "\n"+
				"\t1 nlp askEndPoint false" + "\n"+
				"\t2 nlp LiteralProperty http://www.w3.org/2000/01/rdf-schema#comment" + "\n"+
				"\t2 nlp useFoxLight false" + "\n"+
				"\t3 nlp useFoxLight true";

	
	/**
	 * run GeoLift through command line
	 * @param args
	 * @throws IOException
	 * @author sherif
	 */
	public static void runGeoLiftTSVConfig(String args[]) throws IOException{
		long startTime = System.currentTimeMillis();
		String inputFile	= "";
		String configFile	= "";
		String outputFile	= "";
		
		for(int i=0; i<args.length; i+=2){
			if(args[i].equals("-i") || args[i].toLowerCase().equals("--input")){
				inputFile = args[i+1];
			}
			if(args[i].equals("-c") || args[i].toLowerCase().equals("--config")){
				configFile = args[i+1];
			}
			if(args[i].equals("-o") || args[i].toLowerCase().equals("--output")){
				outputFile = args[i+1];
			}
			if(args[i].equals("-?") || args[i].toLowerCase().equals("--help")){
				logger.info(GeoLift.helpMessage);
				System.exit(0);
			}
		} 
		
		Model startModel =  Reader.readModel(inputFile);
		Multimap<String, Map<String, String>> parameters = TSVConfigReader.getParameters(configFile);
		WorkflowHandler wfh = new WorkflowHandler(startModel, parameters);
		
		if(!outputFile.equals("")){
			wfh.getEnrichedModel().write(new FileWriter(outputFile), "TTL");
		}else{
			wfh.getEnrichedModel().write(System.out, "TTL");
		}
		Long totalTime = System.currentTimeMillis() - startTime;
		logger.info("***** Done in " + totalTime + "ms *****");
	}
	
	
	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String args[]) throws IOException{
		runGeoLiftTSVConfig(args);
	}
}
