package js.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import js.intrumentation.InstrumentationManager;
import js.modularity.JSModuleParser;
import js.modularity.Module;
import js.modularity.ModuleInspectionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class AddInstrumentationCodeAction implements IObjectActionDelegate{
	private ISelection selection;
	private IWorkbenchWindow window;
	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		try {
			window.run(false, true, new IRunnableWithProgress() {
		         public void run(IProgressMonitor monitor)
		            throws InvocationTargetException, InterruptedException {
		            monitor.beginTask("Generate Module Model", 4);
		           
		            IProject selectedProject = (IProject)(((StructuredSelection)selection).getFirstElement());
		            InstrumentationManager.getInstance().initialize();
		            InstrumentationManager.getInstance().setInstrumentFunctions(true);
		            JSModuleParser.getInstance().initialize();
		            
					try {
						//Se inicia el timer
						long time_start, time_end;
						time_start = System.currentTimeMillis();
						//Fin inicio timer
						monitor.worked(2);
			            Module selectedModule = new Module(selectedProject);
						try{
							monitor.subTask("Generating JavaScript Model...");
							JSModuleParser.getInstance().analyzeJavaScriptModule(selectedModule);
							monitor.worked(2);
							InstrumentationManager.getInstance().saveRegisterLogFiles(selectedProject);								
							//Se toma registro del tiempo de ejecucion
							time_end = System.currentTimeMillis();
							System.out.println("the task has taken "+ ( time_end - time_start ) +" milliseconds");
							//Fin registro	
							if(JSModuleParser.getInstance().getProblems().isEmpty()){
								MessageDialog.openInformation(window.getShell(), "Info", "Instrumentation Code was sucessfully saved.");
							}else{
								for(int i = 0; i< JSModuleParser.getInstance().getProblems().size(); i++){
									 ModuleInspectionException e = JSModuleParser.getInstance().getProblems().get(i);
								     MessageDialog.openWarning( window.getShell(), "Warning", "Error parsing file: "+ e.getResourceName() +" -> "+ e.toString());
								}
								MessageDialog.openInformation(window.getShell(), "Info", "Instrumentation Code was saved with errors.");
							}
							
						}catch(Exception e){
							  monitor.subTask("Error...");
						      MultiStatus status = createMultiStatus(e.getLocalizedMessage(), e);
						      // show error dialog
						      ErrorDialog.openError( window.getShell(), "Error", "This is an error", status);
						}
						
						selectedModule.printMetrics();
						
					} catch (Exception e) {
						System.out.println(e);
					}
					
		            monitor.done();
		         }
		      });
			
			
		} catch ( InvocationTargetException | InterruptedException e) {
		      MultiStatus status = createMultiStatus(e.getCause()!=null?e.getCause().getLocalizedMessage():e.getLocalizedMessage(),e);
		      // show error dialog
		      ErrorDialog.openError( window.getShell(), "Error", "This is an error", status);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		window=targetPart.getSite().getWorkbenchWindow();
	}
	
	private static MultiStatus createMultiStatus(String msg, Throwable t) {

	    List<Status> childStatuses = new ArrayList<>();
	    StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

	     for (StackTraceElement stackTrace: stackTraces) {
	      Status status = new Status(IStatus.ERROR,
	          "com.example.e4.rcp.todo", stackTrace.toString());
	      childStatuses.add(status);
	    }

	    MultiStatus ms = new MultiStatus("com.example.e4.rcp.todo",
	        IStatus.ERROR, childStatuses.toArray(new Status[] {}),
	        t.toString(), t);
	    return ms;
	  }

}