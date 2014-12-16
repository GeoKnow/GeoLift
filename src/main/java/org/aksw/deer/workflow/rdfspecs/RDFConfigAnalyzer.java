/**
 * 
 */
package org.aksw.deer.workflow.rdfspecs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RDFConfigAnalyzer {
	
	public static Set<Resource> getModules(Model configModel){
		Set<Resource> result = new HashSet<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?m {?m <" + RDF.type + "> <" + SPECS.Module + "> . }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource module = qs.getResource("?m");
			result.add(module);
		}
		qexec.close() ;
		return result;
	}
	
	public static Resource getLastModuleUriOftype(Resource type, Model configModel){
		List<String> results = new ArrayList<String>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?m {?m <" + RDF.type + "> <" + type.getURI() + "> . }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource module = qs.getResource("?m");
			results.add(module.getURI());
		}
		qexec.close() ;
		Collections.sort(results);
		return ResourceFactory.createResource(results.get(results.size()-1));
	}
	
	public static void main(String args[]){
		System.out.println(getModules(Reader.readModel(args[0])));
		
	}

}