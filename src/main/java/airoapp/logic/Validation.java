package airoapp.logic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.nio.file.Path;



import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;





public class Validation {

    public static void main(String[] args) {
       
    	
    }
    
  
    
    
    /**
     * This method should read the shapes from a local file
     */
    public String readShapes()
    {
        String ttl = "";
        try{
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("shapes.ttl");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream ));
            StringBuffer buffer = new StringBuffer();
            if (reader != null) {
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    line = reader.readLine();            
                }
            }
            ttl = buffer.toString();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return ttl;
    }

    /**
     * From JSON to TTL
     *
     * @param json Receives a JSON with the input parameters.
     * @return Produces a TTL with AIRO class instances.
     */
    public static String generateInstances(String json) {
        String nt="";
        nt += "<http://example.com/ns#mySystem> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/AIRO#AISystem> .";

        try{
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);
            String purpose = (String) jsonObject.get("purpose");   
            String domain = (String) jsonObject.get("domain");
            String user = (String) jsonObject.get("user");
            String environment = (String) jsonObject.get("environment");
            nt += "<http://example.com/ns#mySystem>  <https://w3id.org/AIRO#hasPurpose> <https://w3id.org/AIRO#"+ purpose + "> .";
            nt += "<http://example.com/ns#mySystem>  <https://w3id.org/AIRO#hasDomain> <https://w3id.org/AIRO#"+ domain + "> .";
            nt += "<http://example.com/ns#mySystem>  <https://w3id.org/AIRO#isUsedBy> <https://w3id.org/AIRO#"+ user +"> . ";
            nt += "<http://example.com/ns#mySystem>  <https://w3id.org/AIRO#isUsedInEnvironment> <https://w3id.org/AIRO#" + environment +"> .";   
            // nt += "<https://w3id.org/AIRO#"+purpose+"> a <https://w3id.org/AIRO#BiometricIdentification> .";
           
            System.out.println(nt);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return nt;
    }

    /**
     * Validates a piece of RDF against the shapes
     *
     * @param nt Input data to be validated as NTRIPLES
     * @param path 
     */
    public static boolean validate(String nt) {
        try {
            String shapes = new Validation().readShapes();
            final Model dataModel = ModelFactory.createDefaultModel();
            Model shapeModel = JenaUtil.createDefaultModel();
            try (final InputStream in = new ByteArrayInputStream(shapes.getBytes("UTF-8"))) {
                shapeModel.read(in, null, "TURTLE");
            } catch (Exception e) {
                e.printStackTrace();
            } 
            try (final InputStream in = new ByteArrayInputStream(nt.getBytes("UTF-8"))) {
                dataModel.read(in, null, "N-TRIPLE");
            } catch (Exception e) {
                e.printStackTrace();
            } 
            Resource reportResource = ValidationUtil.validateModel(dataModel, shapeModel, true);
           
            boolean conforms = reportResource.getProperty(SH.conforms).getBoolean();
          //  System.out.println("Conform is "+ conforms);
            
           
           if (!conforms) {
        	    
        	 
        	  String message = JenaUtil.getStringProperty(reportResource, SH.resultMessage);
        	  System.out.println("THIS IS MESSAGE: "+ message);
        	
        	   
        	   	
               String reportPath = "/Users/delaram/Desktop/Phd/Projects/airo-app/src/main/resources/report.ttl";
              // String reportPath = path.toAbsolutePath().toString()+"src/main/resources/report.ttl" ;
               File reportFile = new File(reportPath);
               reportFile.createNewFile();
               OutputStream reportOutputStream = new FileOutputStream(reportFile);
               RDFDataMgr.write(reportOutputStream, reportResource.getModel(), RDFFormat.TURTLE);
               

             /*
        	   String shaclReport = ModelPrinter.get().print(reportResource.getModel());
               FileWriter fw = new FileWriter(reportFile);
               PrintWriter pw = new PrintWriter(fw);
               pw.write(shaclReport);
               pw.close();*/
  
               
       			
       		}
  
            
            return conforms;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
    
    
    
}