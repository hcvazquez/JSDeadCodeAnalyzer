package js.modularity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import js.metrics.Metric;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Module {
	
	/**
	 * File Constants
	 */
	
	public static final String DEPENDENCIES_FOLDER_NAME = "node_modules";
	public static final String PACKAGE_JSON = "package.json";	
	private static final String JS_EXTENSION = "js";
	private static final String MIN_ID = ".min.";
	private static final Object TEST_FOLDER_NAME = "test";
	
	/**
	 * File System Attributes
	 */
	IContainer moduleContainer;
	IFolder dependenciesFolder;
	IFile packageJSON;
	
	/**
	 * Metrics map
	 */
	HashMap<Metric,Double> metrics;
	
	/**
	 * Constructor of the class
	 * @param moduleContainer
	 */
	public Module(IContainer moduleContainer) {
		super();
		this.moduleContainer = moduleContainer;
		/*
		 * Initialize Metrics
		 */
		this.metrics = new HashMap<Metric, Double>();
		metrics.put(Metric.NUM_OF_SOURCEFILES, 0.0);
		metrics.put(Metric.NUM_OF_FUNCTIONS, 0.0);
		metrics.put(Metric.TOTAL_LOC, 0.0);
		metrics.put(Metric.NUM_OF_FUNCTIONS_EMPTIED, 0.0);
		metrics.put(Metric.TOTAL_LOC_REMOVED, 0.0);
	}
	
	/**
	 * Devuelve todos los modulos de los que depende el modulo
	 * @return
	 * @throws CoreException
	 * @throws IOException 
	 */
	private ArrayList<Module> getModuleDependencies(){
		/*
		 * Obtener los nombres de las dependencias del archivo JSON
		 * Buscar todas las carpetas con ese nombre en la carpeta de dependencias
		 * Por cada carpeta crear el modulo y agregarlo a la lista
		 * Devolver la lista de modulos
		 */
		IFolder dependenciesFolder = (IFolder) moduleContainer.findMember(DEPENDENCIES_FOLDER_NAME);
		IFile jsonFile = (IFile) moduleContainer.findMember(PACKAGE_JSON);
		ArrayList<Module> moduleDependencies = new ArrayList<Module>();

		try{
			if(dependenciesFolder!=null && jsonFile!=null){
				String content = new Scanner(new File(jsonFile.getRawLocation().toPortableString())).useDelimiter("\\Z").next();
			    JsonObject obj = new JsonParser().parse(content).getAsJsonObject();
			    if(obj!=null){
			    	JsonObject directDependencies = obj.getAsJsonObject("dependencies");
			    	if(directDependencies!=null){
			    		Set<Map.Entry<String, JsonElement>> entries = directDependencies.entrySet();//will return members of your object
			    		for (Map.Entry<String, JsonElement> entry: entries) {
			    			if(dependenciesFolder.getFolder(entry.getKey()).exists()){
			    				moduleDependencies.add(new Module(dependenciesFolder.getFolder(entry.getKey())));
			    			}
			    		}
			    	}
			    }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return moduleDependencies;
	}
	
	/**
	 * Devuelve todos los archivos propios del modulo
	 * @return
	 * @throws CoreException
	 */
	private ArrayList<SourceFile> getModuleSourceFiles() throws CoreException{	
		/*
		 * Buscar todos los archivos en las carpetas del modulo excepto en node_modules 
		 * Devolver la lista de archivos
		 */	
		ArrayList<SourceFile> moduleFiles = new ArrayList<SourceFile>();
		moduleFiles.addAll(getFilesFromFolder(moduleContainer));
		return moduleFiles;
	}

	/**
	 * Devuelve todos los archivos a partir de una carpeta raiz
	 * Exceptua los que se encuentran en la carpeta node_modules
	 * @param folder
	 * @return files
	 * @throws CoreException
	 */
	private ArrayList<SourceFile> getFilesFromFolder(IContainer folder) throws CoreException{
		ArrayList<SourceFile> folderFiles = new ArrayList<SourceFile>();
		try {
			for(Object selectedResource : folder.members()){
				if(selectedResource instanceof IFile && ((IFile) selectedResource).getFileExtension()!=null && ((IFile) selectedResource).getFileExtension().equalsIgnoreCase(JS_EXTENSION) && ((IFile) selectedResource).getName().indexOf(MIN_ID)==-1){
					folderFiles.add(new SourceFile((IFile) selectedResource));
				}else if(selectedResource instanceof IFolder 
						&& !DEPENDENCIES_FOLDER_NAME.equals(((IFolder)selectedResource).getName()) 
						&& !TEST_FOLDER_NAME.equals(((IFolder)selectedResource).getName())){
					folderFiles.addAll(getFilesFromFolder((IFolder)selectedResource));
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return folderFiles;
	};
	
	/**
	 * Inspecciona un determinado modulo
	 * @param mainModule
	 * @throws ModuleInspectionException
	 */
	public void inspectJavaScriptModule(Module mainModule){
			for(Module dependencie : mainModule.getModuleDependencies()) {
				inspectJavaScriptModuleFolder(dependencie);
				metricSum(Metric.NUM_OF_FUNCTIONS, dependencie.getMetrics().get(Metric.NUM_OF_FUNCTIONS));
				metricSum(Metric.TOTAL_LOC, dependencie.getMetrics().get(Metric.TOTAL_LOC));
				metricSum(Metric.NUM_OF_FUNCTIONS_EMPTIED, dependencie.getMetrics().get(Metric.NUM_OF_FUNCTIONS_EMPTIED));
				metricSum(Metric.TOTAL_LOC_REMOVED, dependencie.getMetrics().get(Metric.TOTAL_LOC_REMOVED));
			}
	}
	
	/**
	 * Inspecciona una determinada carpeta
	 * @param mainModule
	 * @throws ModuleInspectionException
	 */
	public void inspectJavaScriptModuleFolder(Module module){
		ArrayList<SourceFile> sourceFiles = null;
		try {
			sourceFiles = module.getModuleSourceFiles();
		} catch (CoreException e) {
			JSModuleParser.getInstance().addProblem (new ModuleInspectionException(e,module.getModuleContainer().getRawLocation().toPortableString()));
		}
		if(sourceFiles!=null){
			for(SourceFile sourceFile:sourceFiles){
				metricSum(Metric.NUM_OF_SOURCEFILES,1.0);
				try {
					sourceFile.inspect();
					metricSum(Metric.NUM_OF_FUNCTIONS,sourceFile.getMetrics().get(Metric.NUM_OF_FUNCTIONS));
					metricSum(Metric.TOTAL_LOC,sourceFile.getMetrics().get(Metric.TOTAL_LOC));
					metricSum(Metric.NUM_OF_FUNCTIONS_EMPTIED,sourceFile.getMetrics().get(Metric.NUM_OF_FUNCTIONS_EMPTIED));
					metricSum(Metric.TOTAL_LOC_REMOVED,sourceFile.getMetrics().get(Metric.TOTAL_LOC_REMOVED));
				} catch (Exception e) {
					JSModuleParser.getInstance().addProblem(new ModuleInspectionException(e,sourceFile.getFullPath()));
				}
			}
			inspectJavaScriptModule(module);
		}
	}

	public IContainer getModuleContainer() {
		return moduleContainer;
	}
	
	public void metricSum(Metric metric, Double valueToAdd){
		metrics.put(metric, metrics.get(metric)+valueToAdd);
	}

	public void printMetrics() {
		System.out.println("-------METRICS-------");
		for(Metric metric:metrics.keySet()){
			System.out.println(metric.getName()+": "+metrics.get(metric));
		}
		System.out.println("-----END METRICS-----");
		
	}

	public HashMap<Metric, Double> getMetrics() {
		return metrics;
	}
}
