/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.deer.helper.datastructure.Tree;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.aksw.deer.workflow.rdfspecs.RDFConfigWriter;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * @author sherif
 *
 */
public class ComplexPipeLineLearner implements PipelineLearner{
	private static final Logger logger = Logger.getLogger(ComplexPipeLineLearner.class.getName());
	public double penaltyWeight = 0.5;// [0, 1]
	private int datasetCounter = 1;
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	public Tree<RefinementNode> refinementTreeRoot = new Tree<RefinementNode>(new RefinementNode());
	RDFConfigWriter configWriter = new RDFConfigWriter();
	public int iterationNr = 0;

	private final double 	MAX_FITNESS_THRESHOLD = 1; 
	private final long 	MAX_TREE_SIZE = 50;
	public final double 	CHILDREN_PENALTY_WEIGHT   = 1; 
	public final double 	COMPLEXITY_PENALTY_WEIGHT = 1;
	
	private DeerModule leftModule = null;


	/**
	 * Contractors
	 *@author sherif
	 */
	public ComplexPipeLineLearner() {
		sourceModel = ModelFactory.createDefaultModel();
		targetModel = ModelFactory.createDefaultModel();
	}

	ComplexPipeLineLearner(Model source, Model target){
		sourceModel  = source;
		targetModel  = target;
	}

	ComplexPipeLineLearner(Model source, Model target, double penaltyWeight){
		this(source, target);
		this.penaltyWeight = penaltyWeight;
	}


	public RefinementNode runComplex(){
		refinementTreeRoot = createRefinementTreeRoot();
		RefinementNode left = getLeftNode(refinementTreeRoot);
		RefinementNode right = getRightNode(refinementTreeRoot);
		createCloneMergeNode(refinementTreeRoot, left, right);

		refinementTreeRoot.print();
//		logger.info("Most promising node: " + mostPromisingNode.getValue());
//		iterationNr ++;
//		while((mostPromisingNode.getValue().fitness) < MAX_FITNESS_THRESHOLD	 
//				&& refinementTreeRoot.size() <= MAX_TREE_SIZE)
//		{
//			iterationNr++;
//			mostPromisingNode = expandNode(mostPromisingNode);
//			mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
//			refinementTreeRoot.print();
//			if(mostPromisingNode.getValue().fitness == -Double.MAX_VALUE){
//				// no better solution can be found
//				break;
//			}
//			logger.info("Most promising node: " + mostPromisingNode.getValue());
//		}
//		logger.info("----------------------------------------------");
//		RefinementNode bestSolution = getMostPromisingNode(refinementTreeRoot, 0).getValue();
//		//		logger.info("Best Solution: " + bestSolution.toString());
//		//		System.out.println("===== Output Config =====");
//		//		bestSolution.configModel.write(System.out,"TTL");
//		//		System.out.println("===== Output Dataset =====");
//		//		bestSolution.outputModel.write(System.out,"TTL");
//		//		System.out.println("===== Output Config =====");
//		//		mostPromisingNode.getValue().configModel.write(System.out,"TTL");
//		//		System.out.println("===== Output Dataset =====");
//		//		mostPromisingNode.getValue().outputModel.write(System.out,"TTL");
//		bestSolution.configModel = setIOFiles(bestSolution.configModel, "inputFile.ttl", "outputFile.ttl"); 
//		return bestSolution;
		return null;
	}




	/**
	 * @param refinementTreeRoot2
	 * @param left
	 * @param right
	 * @author sherif
	 */
	private Tree<RefinementNode> createCloneMergeNode(Tree<RefinementNode> root, RefinementNode left, RefinementNode right) {
		if(left == null && right == null){
			return root;
		}
		if(right == null){
			root.addChild(new Tree<RefinementNode>(left));
		}else if(left == null){
			root.addChild(new Tree<RefinementNode>(right));
		}else{
			RefinementNode clone = new RefinementNode(module, fitness, inputModel, outputModel, inputDataset, outputDataset, configModel)
			root.addChild(new Tree<RefinementNode>(right));
		}
		
		
		return root;
	}

	public RefinementNode run(){
		refinementTreeRoot = createRefinementTreeRoot();
		refinementTreeRoot = expandNode(refinementTreeRoot);
		Tree<RefinementNode> mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
		refinementTreeRoot.print();
		logger.info("Most promising node: " + mostPromisingNode.getValue());
		iterationNr ++;
		while((mostPromisingNode.getValue().fitness) < MAX_FITNESS_THRESHOLD	 
				&& refinementTreeRoot.size() <= MAX_TREE_SIZE)
		{
			iterationNr++;
			mostPromisingNode = expandNode(mostPromisingNode);
			mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
			refinementTreeRoot.print();
			if(mostPromisingNode.getValue().fitness == -Double.MAX_VALUE){
				// no better solution can be found
				break;
			}
			logger.info("Most promising node: " + mostPromisingNode.getValue());
		}
		logger.info("----------------------------------------------");
		RefinementNode bestSolution = getMostPromisingNode(refinementTreeRoot, 0).getValue();
		//		logger.info("Best Solution: " + bestSolution.toString());
		//		System.out.println("===== Output Config =====");
		//		bestSolution.configModel.write(System.out,"TTL");
		//		System.out.println("===== Output Dataset =====");
		//		bestSolution.outputModel.write(System.out,"TTL");
		//		System.out.println("===== Output Config =====");
		//		mostPromisingNode.getValue().configModel.write(System.out,"TTL");
		//		System.out.println("===== Output Dataset =====");
		//		mostPromisingNode.getValue().outputModel.write(System.out,"TTL");
		bestSolution.configModel = setIOFiles(bestSolution.configModel, "inputFile.ttl", "outputFile.ttl"); 
		return bestSolution;
	}

	private Tree<RefinementNode> createRefinementTreeRoot(){
		Resource outputDataset  = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
		Model config = ModelFactory.createDefaultModel();
		double f = -Double.MAX_VALUE;
		RefinementNode initialNode = new RefinementNode(null,f,sourceModel,sourceModel,outputDataset,outputDataset,config);
		return new Tree<RefinementNode>(null,initialNode, null);
	}

	private Tree<RefinementNode> expandNode(Tree<RefinementNode> root) {
		for( DeerModule module : MODULES){
			Model inputModel = root.getValue().outputModel;
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			Resource inputDataset  = root.getValue().outputDataset;
			Model configMdl = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				// mark as dead end, fitness = -2
				configMdl = root.getValue().configModel;
				node = new RefinementNode( module, -2, sourceModel, sourceModel, inputDataset, inputDataset, configMdl);
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				double fitness;
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					fitness = -2;
				}else{
					//					fitness = computeFitness(currentMdl, targetModel);
					fitness = computeFMeasure(currentMdl, targetModel);
				}
				Resource outputDataset = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
				configMdl = configWriter.addModule(root.getValue().configModel, module, parameters, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, root.getValue().outputModel, currentMdl, inputDataset, outputDataset, configMdl);
			}
			root.addChild(new Tree<RefinementNode>(node));
		}
		return root;
	}

	private RefinementNode getLeftNode(Tree<RefinementNode> root) {
		RefinementNode promisingNode = null; 
		for( DeerModule module : MODULES){
			Model inputModel = root.getValue().outputModel;
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			Resource inputDataset  = root.getValue().outputDataset;
			Model configMdl = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				continue; // Dead node
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					continue; // Dead node
				}else{
					double fitness = computeFMeasure(currentMdl, targetModel);
					Resource outputDataset = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
					configMdl = configWriter.addModule(root.getValue().configModel, module, parameters, inputDataset, outputDataset);
					node = new RefinementNode(module, fitness, root.getValue().outputModel, currentMdl, inputDataset, outputDataset, configMdl);
					if(promisingNode == null || promisingNode.fitness < fitness){
						promisingNode = node;
						leftModule = module;
					}
				}
			}
		}
//		root.addChild(new Tree<RefinementNode>(promisingNode));
		return promisingNode;
	}
	
	private RefinementNode getRightNode(Tree<RefinementNode> root) {
		RefinementNode promisingNode = null; 
		for( DeerModule module : MODULES){
			if(module.getClass().equals(leftModule.getClass())){
				continue;
			}
			Model inputModel = root.getValue().outputModel;
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			Resource inputDataset  = root.getValue().outputDataset;
			Model configMdl = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				continue; // Dead node
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					continue; // Dead node
				}else{
					double fitness = computeFMeasure(currentMdl, targetModel);
					Resource outputDataset = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
					configMdl = configWriter.addModule(root.getValue().configModel, module, parameters, inputDataset, outputDataset);
					node = new RefinementNode(module, fitness, root.getValue().outputModel, currentMdl, inputDataset, outputDataset, configMdl);
					if(promisingNode == null || promisingNode.fitness < fitness){
						promisingNode = node;
					}
				}
			}
		}
//		root.addChild(new Tree<RefinementNode>(promisingNode));
		return promisingNode;
	}


	/**
	 * Compute the fitness of the generated model by current specs
	 * Simple implementation is difference between current and target 
	 * @return
	 * @author sherif
	 */
	double computeFitness(Model currentModel, Model targetModel){
		long t_c = targetModel.difference(currentModel).size();
		long c_t = currentModel.difference(targetModel).size();
		System.out.println("targetModel.difference(currentModel).size() = " + t_c);
		System.out.println("currentModel.difference(targetModel).size() = " + c_t);
		return 1- ((double)(t_c + c_t) / (double)(currentModel.size() + targetModel.size()));
	}

	double computeFMeasure(Model currentModel, Model targetModel){
		double p = computePrecision(currentModel, targetModel);
		double r = computeRecall(currentModel, targetModel);
		if(p == 0 && r == 0){
			return 0;
		}
		return 2 * p * r / (p +r);

	}

	double computePrecision (Model currentModel, Model targetModel){
		return (double) currentModel.intersection(targetModel).size() / (double) currentModel.size();
	}

	double computeRecall(Model currentModel, Model targetModel){
		return (double) currentModel.intersection(targetModel).size() / (double) targetModel.size();
	}

	private Tree<RefinementNode> getMostPromisingNode(Tree<RefinementNode> root, double penaltyWeight){
		// trivial case
		if(root.getchildren() == null || root.getchildren().size() == 0){
			return root;
		}
		// get mostPromesyChild of children
		Tree<RefinementNode> mostPromesyChild = new Tree<RefinementNode>(new RefinementNode());
		for(Tree<RefinementNode> child : root.getchildren()){
			if(child.getValue().fitness >= 0){
				Tree<RefinementNode> promesyChild = getMostPromisingNode(child, penaltyWeight);
				double newFitness;
				newFitness = promesyChild.getValue().fitness - penaltyWeight * computePenality(promesyChild);
				if( newFitness > mostPromesyChild.getValue().fitness  ){
					mostPromesyChild = promesyChild;
				}
			}
		}
		// return the argmax{root, mostPromesyChild}
		if(penaltyWeight > 0){
			return mostPromesyChild;
		}else if(root.getValue().fitness >= mostPromesyChild.getValue().fitness){
			return root;
		}else{
			return mostPromesyChild;
		}
	}


	/**
	 * @return
	 * @author sherif
	 */
	private double computePenality(Tree<RefinementNode> promesyChild) {
		long childrenCount = promesyChild.size() - 1;
		double childrenPenalty = (CHILDREN_PENALTY_WEIGHT * childrenCount) / refinementTreeRoot.size();
		long level = promesyChild.level();
		double complextyPenalty = (COMPLEXITY_PENALTY_WEIGHT * level) / refinementTreeRoot.depth();
		return  childrenPenalty + complextyPenalty;
	}

	public static void main(String args[]) throws IOException{
				trivialRun(args);
//		evaluation(args, false, 1);
	}

	public static void trivialRun(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		ComplexPipeLineLearner learner = new ComplexPipeLineLearner();
		learner.sourceModel  = Reader.readModel(sourceUri);
		learner.targetModel = Reader.readModel(targetUri);
		long start = System.currentTimeMillis();
		learner.runComplex();
		long end = System.currentTimeMillis();
		logger.info("Done in " + (end - start) + "ms");
	}

	public static void evaluation(String args[], boolean isBatch, int max) throws IOException{
		String folder = args[0];
		String results = "ModuleCount\tTime\tTreeSize\tIterationNr\tP\tR\tF\n";
		for(int i = 1 ; i <= max; i++){
			ComplexPipeLineLearner learner = new ComplexPipeLineLearner();
			if(isBatch){
				folder = folder + i;
			}
			learner.sourceModel  = Reader.readModel(folder + "/input.ttl");
			learner.targetModel  = Reader.readModel(folder + "/output.ttl");
			long start = System.currentTimeMillis();
			RefinementNode bestSolution = learner.run();
			long end = System.currentTimeMillis();
			long time = end - start;
			results += i + "\t" + time + "\t" + 
					learner.refinementTreeRoot.size() + "\t" + 
					learner.iterationNr + "\t" + 
					//					bestSolution.fitness + "\t" +
					learner.computePrecision(bestSolution.outputModel, targetModel) + "\t" + 
					learner.computeRecall(bestSolution.outputModel, targetModel) + "\t" +
					learner.computeFMeasure
					(bestSolution.outputModel, targetModel);
			Writer.writeModel(bestSolution.configModel, "TTL", folder + "/self_config.ttl");
			//			bestSolution.outputModel.write(System.out,"TTL");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(results);
			//			break;
		}
		System.out.println(results);
	}


	Model setIOFiles(final Model sConfig, String inputFile, String outputFile){
		Model resultModel = ModelFactory.createDefaultModel();
		resultModel = resultModel.union(sConfig);
		List<String> datasets = new ArrayList<String>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {?d <" + RDF.type + "> <" + SPECS.Dataset + ">.} ";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, resultModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			datasets.add(dataset.toString());
		}
		qexec.close() ;
		Collections.sort(datasets);
		Resource inputDataset = ResourceFactory.createResource(datasets.get(0));
		Resource outputDataset = ResourceFactory.createResource(datasets.get(datasets.size()-1));
		resultModel.add(inputDataset, SPECS.inputFile, inputFile);
		resultModel.add(outputDataset, SPECS.outputFile, outputFile);
		resultModel.setNsPrefixes(sConfig);
		return resultModel;
	}

}
