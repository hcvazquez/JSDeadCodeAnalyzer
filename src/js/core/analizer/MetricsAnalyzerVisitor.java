package js.core.analizer;

import js.metrics.Metric;
import js.modularity.SourceFile;

import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;

public class MetricsAnalyzerVisitor extends ASTVisitor {

	protected JavaScriptUnit unit;
	protected SourceFile sourceFile;

	public MetricsAnalyzerVisitor(SourceFile sourceFile, JavaScriptUnit unit) {
		this.sourceFile = sourceFile;
		this.unit = unit;
	}
	
	@Override
	public boolean visit(FunctionDeclaration functionDcl) {
//		if(functionDcl.resolveBinding()!=null){
			sourceFile.metricSum(Metric.NUM_OF_FUNCTIONS, 1.0);
			sourceFile.metricSum(Metric.TOTAL_LOC, (double) (unit.getLineNumber(functionDcl.getStartPosition()+functionDcl.getLength()-1)-unit.getLineNumber(functionDcl.getStartPosition())));
			if(functionDcl.getBody()==null || functionDcl.getBody().statements()==null ||functionDcl.getBody().statements().isEmpty()){
				sourceFile.metricSum(Metric.NUM_OF_FUNCTIONS_EMPTIED,1.0);
			//sourceFile.metricSum(Metric.TOTAL_LOC_REMOVED, (double)unit.getLineNumber(functionDcl.getBody().getStartPosition()+functionDcl.getBody().getLength()-1)-unit.getLineNumber(functionDcl.getBody().getStartPosition()));
			}
//		}
		return super.visit(functionDcl);
	}

}
