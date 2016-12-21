package js.metrics;

public enum Metric{
		
		NUM_OF_SOURCEFILES("numOfSourceFiles"), NUM_OF_FUNCTIONS("numOfFunctions"), TOTAL_LOC("totalLOC"),
		NUM_OF_SOURCEFILES_MODIFIED("numOfSourceFilesModified"), NUM_OF_FUNCTIONS_EMPTIED("numOfFunctionsEmptied"), TOTAL_LOC_REMOVED("totalLOCRemoved");

		private String name;
		
		private Metric(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}	
}

