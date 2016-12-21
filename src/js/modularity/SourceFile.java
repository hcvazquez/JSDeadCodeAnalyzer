package js.modularity;

import java.util.HashMap;

import js.core.analizer.MetricsAnalyzerVisitor;
import js.core.analizer.ModuleAnalyzerVisitor;
import js.intrumentation.InstrumentationManager;
import js.metrics.Metric;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.rewrite.ASTRewrite;

public class SourceFile {
	
	protected IFile sourceFile;
	protected HashMap<Metric,Double> metrics;

	/**
	 * The contructor of the class
	 * @param sourceFile
	 */
	public SourceFile(IFile sourceFile) {
		super();
		this.sourceFile = sourceFile;
		/*
		 * Initialize metrics
		 */
		this.metrics = new HashMap<Metric, Double>();
		this.metrics.put(Metric.NUM_OF_FUNCTIONS, 0.0);
		this.metrics.put(Metric.TOTAL_LOC, 0.0);
		this.metrics.put(Metric.NUM_OF_FUNCTIONS_EMPTIED, 0.0);
		this.metrics.put(Metric.TOTAL_LOC_REMOVED, 0.0);
	}
	
	/**
	 * Inspecciona un determinado archivo del modulo
	 * @param selectedFile
	 * @param module
	 * @throws BadLocationException 
	 * @throws MalformedTreeException 
	 * @throws CoreException 
	 */
	public void inspect() throws MalformedTreeException, BadLocationException, CoreException{
		refresh();
		IJavaScriptUnit unit = JavaScriptCore.createCompilationUnitFrom(sourceFile);
		unit.open(null);
		unit.becomeWorkingCopy(null);
		System.out.println("parse "+ unit.getDisplayName());
		JavaScriptUnit parse = JSModuleParser.parse(unit);
		//create a ASTRewrite
		AST ast = parse.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		ModuleAnalyzerVisitor visitor = new ModuleAnalyzerVisitor(this, parse,rewriter);
		parse.accept(visitor);
		
		saveChanges(rewriter,unit);
		refresh();
	}
	

	public void inspectForMetrics(){
		try {
			refresh();
			IJavaScriptUnit unit = JavaScriptCore.createCompilationUnitFrom(sourceFile);
			System.out.println("parse "+ unit.getDisplayName());
			JavaScriptUnit parse = JSModuleParser.parse(unit);
			MetricsAnalyzerVisitor visitor = new MetricsAnalyzerVisitor(this, parse);
			parse.accept(visitor);
		}catch (CoreException e) {
			JSModuleParser.getInstance().addProblem(new ModuleInspectionException(e,getFullPath()));
		}
	}


	/**
	 * Guarda los cambios hechos en el AST
	 * @param rewriter
	 * @param unit
	 * @throws JavaScriptModelException
	 * @throws IllegalArgumentException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	private void saveChanges(ASTRewrite rewriter, IJavaScriptUnit unit) throws JavaScriptModelException, IllegalArgumentException, MalformedTreeException, BadLocationException {
		TextEdit edits = rewriter.rewriteAST();
		// apply the text edits to the compilation unit
		Document document = new Document(unit.getSource());		 
		edits.apply(document);
	 	// this is the code for adding statements
		unit.getBuffer().setContents(document.get());
		unit.commitWorkingCopy(true, null);
		unit.save(null, false);
		System.out.println("done");
	}

	/**
	 * return the IFile source
	 * @return
	 */
	public IFile getSourceFile() {
		return sourceFile;
	}
	
	/**
	 * refresh the local workspace copy
	 * @throws CoreException
	 */
	public void refresh() throws CoreException{
		sourceFile.refreshLocal(IFile.DEPTH_INFINITE, null);
	}
	
	/**
	 * return the absolute full path
	 * @return
	 */
	public String getFullPath(){
		return sourceFile.getRawLocation().toPortableString();
	}

	public HashMap<Metric,Double> getMetrics(){
		return metrics;
	}
	
	public void metricSum(Metric metric, Double valueToAdd){
		metrics.put(metric, metrics.get(metric)+valueToAdd);
	}
	
	public String getSourceId(){
		return getFullPath();
	}
	
	public void printMetrics() {
		System.out.println("-------METRICS-------");
		for(Metric metric:metrics.keySet()){
			System.out.println(metric.getName()+": "+metrics.get(metric));
		}
		System.out.println("-----END METRICS-----");
		
	}
	
	public String getStringMetrics() {
		String result = "-------METRICS-------";
		for(Metric metric:metrics.keySet()){
			result+="/r/n";
			result+=metric.getName()+": "+metrics.get(metric);
		}
		result+="/r/n";
		result+="-----END METRICS-----";
		return result;
	}
}
