package js.intrumentation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
 
public class LogComparator {
  
    public static ArrayList<String> cargarArchivo(String archivo) throws FileNotFoundException, IOException {
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
 
    public ArrayList<String> compareLogs(String logLinesAddedPath, String logLinesALoggedPath ) throws IOException {
        ArrayList<String> logLinesAdded = cargarArchivo(logLinesAddedPath);
        ArrayList<String> logLinesALogged = cargarArchivo(logLinesALoggedPath);
        
        logLinesALogged = preprocesing(logLinesALogged);
        
        for(String lineLogged:logLinesALogged){
        	if(logLinesAdded.contains(lineLogged)){
        		logLinesAdded.remove(lineLogged);
        	}
        }
        
        ArrayList<String> result = new ArrayList<String>();
        for(String deadFunction:logLinesAdded){
        	result.add(deadFunction);
        	System.out.println(deadFunction);
        }
        
        return result;
        //saveDiffLog(result,"C:/Users/Hernan/Desktop/diffOrigin.log");
        	
    }
    
	private static ArrayList<String> preprocesing(ArrayList<String> logLinesALogged) {
		ArrayList<String> result = new ArrayList<String>();
        for(String lineLogged:logLinesALogged){
        	String[] parts = lineLogged.split("PROFILING_INFO");
        	if(parts.length>1){
        		result.add(parts[1]);
        	}
        }
        return result;
	}

	public static void saveDiffLog(ArrayList<String> result, String path){
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
            for(String line:result){
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
   
}

