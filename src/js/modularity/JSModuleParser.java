package js.modularity;

import java.util.ArrayList;

import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;


public class JSModuleParser{
	protected ArrayList<ModuleInspectionException> problems;
	private static JSModuleParser instance;
	
	private JSModuleParser(){
		super();
		problems = new ArrayList<ModuleInspectionException>();
	}
	
	public static JSModuleParser getInstance(){
		if(instance==null){
			instance = new JSModuleParser();
		}
		return instance;
	}
	
	public void initialize(){
		instance = new JSModuleParser();
	}
	

	/**
	 * Reads a ICompilationUnit and creates the AST DOM for manipulating the
	 * Java source file
	 * 
	 * @param unit
	 * @return
	 */

	public static JavaScriptUnit parse(IJavaScriptUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (JavaScriptUnit) parser.createAST(null);
	}

	public ArrayList<ModuleInspectionException> getProblems() {
		return problems;
	}
	
	public void analyzeJavaScriptModule(Module module) {
		module.inspectJavaScriptModule(module);
	}
	
	public void analyzeJavaScriptModuleFolder(Module module) {
		module.inspectJavaScriptModuleFolder(module);
	}

	public void addProblem(ModuleInspectionException problem) {
		this.problems.add(problem);
	}

	public void analyzeSingleFile(SourceFile sourceFile) {
		sourceFile.inspectForMetrics();
		
	}
	
}
