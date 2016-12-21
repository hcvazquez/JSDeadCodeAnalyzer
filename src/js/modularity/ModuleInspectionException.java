package js.modularity;

public class ModuleInspectionException extends Exception {
	
	String resourceName;
	// Constructor
    public ModuleInspectionException(Exception e, String resourceName){
    	super(e);
    	this.resourceName = resourceName;
    }
    
    // Excepcion: Error Provocado
    public String getMessage(){
        return "Error parsing file "+resourceName+".";
    }

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
}
