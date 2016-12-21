package js.core.analizer;

import js.intrumentation.InstrumentationManager;
import js.metrics.Metric;
import js.modularity.SourceFile;

import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.core.dom.rewrite.ASTRewrite;

public class ModuleAnalyzerVisitor extends ASTVisitor {

	protected JavaScriptUnit unit;
	protected ASTRewrite rewriter;
	protected SourceFile sourceFile;

	public ModuleAnalyzerVisitor(SourceFile sourceFile, JavaScriptUnit unit, ASTRewrite rewriter) {
		this.sourceFile = sourceFile;
		this.unit = unit;
		this.rewriter = rewriter;
	}
	
	@Override
	public boolean visit(FunctionDeclaration functionDcl) {
//		if(functionDcl.resolveBinding()!=null){
			sourceFile.getMetrics().put(Metric.NUM_OF_FUNCTIONS, sourceFile.getMetrics().get(Metric.NUM_OF_FUNCTIONS)+1.0);
			sourceFile.getMetrics().put(Metric.TOTAL_LOC, sourceFile.getMetrics().get(Metric.TOTAL_LOC)+(unit.getLineNumber(functionDcl.getStartPosition()+functionDcl.getLength()-1)-unit.getLineNumber(functionDcl.getStartPosition())));
			if(InstrumentationManager.getInstance().instrumentFunctions()){
				InstrumentationManager.getInstance().addFileInstrumented(sourceFile.getSourceId());
				InstrumentationManager.getInstance().addInstrumentationCode(sourceFile, functionDcl, rewriter, unit);
			}
			if(InstrumentationManager.getInstance().removeFunctions()){
				InstrumentationManager.getInstance().addFileInstrumented(sourceFile.getSourceId());
				InstrumentationManager.getInstance().removeFunctionWithoutInstrumentation(sourceFile, functionDcl, rewriter, unit);
			}
			if(functionDcl.getBody()==null || functionDcl.getBody().statements()==null ||functionDcl.getBody().statements().isEmpty()){
				sourceFile.getMetrics().put(Metric.NUM_OF_FUNCTIONS_EMPTIED,1.0);
			}
//		}
		return super.visit(functionDcl);
	}

}
