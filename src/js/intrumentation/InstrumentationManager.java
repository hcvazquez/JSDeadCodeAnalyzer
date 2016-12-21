package js.intrumentation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import js.metrics.Metric;
import js.modularity.SourceFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.Block;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.Statement;
import org.eclipse.wst.jsdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.wst.jsdt.core.dom.rewrite.ListRewrite;


public class InstrumentationManager {
	
	private static final String LABEL = "PROFILING_INFO";
	private static final String INSTRUMENTATION_REGISTER_FILE = "register";
	private static final String INSTRUMENTED_FILES = "files";
	private static final String REMOVED_FUNCTIONS = "removedFunctions";
	private static InstrumentationManager instance;
	private ArrayList<String> instrumentationLines;
	private ArrayList<String> instrumentationLinesForRemove;
	private LogComparator logComparator;
	
	private boolean removeFunctions;
	private boolean instrumentFunctions;
	
	private ArrayList<String> instrumentedFiles;
	
	private InstrumentationManager(){
		instrumentationLines = new ArrayList<String>();
		logComparator = new LogComparator();
		instrumentationLinesForRemove = new ArrayList<String>();
		instrumentedFiles = new ArrayList<String>();
		removeFunctions = false;
		instrumentFunctions = false;
	}
	
	public static InstrumentationManager getInstance(){
		if(instance==null){
			instance = new InstrumentationManager();
		}
		return instance;
	}
	
	public void initialize(){
		instrumentationLines = new ArrayList<String>();
		logComparator = new LogComparator();
		instrumentationLinesForRemove = new ArrayList<String>();
		removeFunctions = false;
		instrumentFunctions = false;
	}
	
	public void addInstrumentationLine(String line){
		instrumentationLines.add(line);
	}

	public ArrayList<String> getInstrumentationLines() {
		return instrumentationLines;
	}
	
	public void saveLog(String path, Collection<String> data){
		FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
        	if(path.indexOf(".log")!=-1){
        		fichero = new FileWriter(path);
        	}else{
        		fichero = new FileWriter(path+".log");
        	}
            pw = new PrintWriter(fichero);
            for(String line:data){
            	pw.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
	}

	public void generateRemoveInformation(String logLinesAddedPath, String logLinesALoggedPath) {
		try {
			instrumentationLinesForRemove = logComparator.compareLogs(logLinesAddedPath, logLinesALoggedPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
    public ArrayList<String> cargarArchivo(String archivo) throws FileNotFoundException, IOException {
        String cadena;
        FileReader f = new FileReader(archivo);
        BufferedReader b = new BufferedReader(f);
        ArrayList<String> result = new ArrayList<String>();
        while((cadena = b.readLine())!=null) {
        	//String line = cadena.concat("");
        	result.add(cadena.concat(""));
        }
        b.close();
        return result;
    }

	public ArrayList<String> getInstrumentationLinesForRemove() {
		return instrumentationLinesForRemove;
	}

	public String getInstrumentationLine(String unitIdentifier, int startLine,
			String functionName) {

		return "unitIdentifier:" + unitIdentifier + " lineNumber:"+startLine+" functionName:"+functionName;
	}

	public String getInstrumentationCode(String unitIdentifier, int startLine,
			String functionName) {
		String instrumentationLine = getInstrumentationLine(unitIdentifier, startLine, functionName);
		String instrumentationCode = "var log4js = require('log4js');"+
									 "var logger = log4js.getLogger();"+
									 "logger.info(\""+LABEL+instrumentationLine+LABEL+"\");";
		return instrumentationCode;
	}

	public void saveRegisterLogFiles(IProject selectedProject) {
		try {
			IFolder folder = prepare(selectedProject);
			saveLog(folder.getRawLocation().makeAbsolute().toPortableString()+"/"+INSTRUMENTATION_REGISTER_FILE,instrumentationLines);
			saveLog(folder.getRawLocation().makeAbsolute().toPortableString()+"/"+INSTRUMENTED_FILES,instrumentedFiles);
			folder.refreshLocal(IFolder.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveRemovedFunctions(IProject selectedProject) {
		try {
			IFolder folder = prepare(selectedProject);
			saveLog(folder.getRawLocation().makeAbsolute().toPortableString()+"/"+REMOVED_FUNCTIONS,instrumentationLinesForRemove);
			folder.refreshLocal(IFolder.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IFolder prepare(IProject selectedProject) throws CoreException {
		IFile file = selectedProject.getFile("instrumentationLogs/hack.log");//This is only a hack to create the instrumentationLog folder
		IFolder folder = (IFolder) file.getParent();
	    if (!folder.exists()) {
			folder.create(false, true, null);
	    }
		return folder;
	}

	public void addFileInstrumented(String fileId) {
		instrumentedFiles.add(fileId);
	}
	
	public boolean wasInstrumented(String fileId){
		if(instrumentedFiles.contains(fileId)){
			return true;
		}
		return false;
	}

	public void loadFileNames(String result0) throws IOException {
		String cadena;
        FileReader f = new FileReader(result0);
        BufferedReader b = new BufferedReader(f);
        while((cadena = b.readLine())!=null) {
        	//String line = cadena.concat("");
        	instrumentedFiles.add(cadena.concat(""));
        }
        b.close();
	}

	public void addInstrumentationCode(SourceFile sourceFile, FunctionDeclaration functionDcl, ASTRewrite rewriter, JavaScriptUnit unit) {
		Block block = functionDcl.getBody();
		ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		//notice here, we just create a string placeholder, and string is simply as empty
		int startLine = unit.getLineNumber(functionDcl.getStartPosition());
		String functionName = functionDcl.getName()!=null?functionDcl.getName().getFullyQualifiedName():"";
		Statement placeHolder = (Statement) rewriter.createStringPlaceholder(
				getInstrumentationCode(sourceFile.getSourceId(), startLine, functionName), ASTNode.EMPTY_STATEMENT);	
		listRewrite.insertFirst(placeHolder, null);
		InstrumentationManager.getInstance().addInstrumentationLine(
				getInstrumentationLine(sourceFile.getSourceId(), startLine, functionName));
		
	}
	
	public void removeFunctionWithoutInstrumentation(SourceFile sourceFile, FunctionDeclaration functionDcl, ASTRewrite rewriter, JavaScriptUnit unit){
		int startLine = unit.getLineNumber(functionDcl.getStartPosition());
		String functionName = functionDcl.getName()!=null?functionDcl.getName().getFullyQualifiedName():"";
		if(getInstrumentationLinesForRemove().contains(getInstrumentationLine(sourceFile.getSourceId(), startLine, functionName))){
			try{
				sourceFile.metricSum(Metric.NUM_OF_FUNCTIONS_EMPTIED,1.0);
				sourceFile.metricSum(Metric.TOTAL_LOC_REMOVED, (double)unit.getLineNumber(functionDcl.getBody().getStartPosition()+functionDcl.getBody().getLength()-1)-unit.getLineNumber(functionDcl.getBody().getStartPosition()));
				for(Object statement:functionDcl.getBody().statements()){
					rewriter.remove((ASTNode) statement, null);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public boolean removeFunctions() {
		return removeFunctions;
	}

	public void setRemoveFunctions(boolean removeFunctions) {
		this.removeFunctions = removeFunctions;
	}

	public boolean instrumentFunctions() {
		return instrumentFunctions;
	}

	public void setInstrumentFunctions(boolean instrumentFunctions) {
		this.instrumentFunctions = instrumentFunctions;
	}

}
