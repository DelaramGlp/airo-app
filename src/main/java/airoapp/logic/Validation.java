package airoapp.logic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class Validation {

    public static void main(String[] args) {
        testGood();
        testBad();
    }
    
    public static void testGood()
    {
        String test = "";
        test += "<https://test> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/AIRO#AISystem> .";
        test += "<https://test> <https://w3id.org/AIRO#hasPurpose> <https://w3id.org/AIRO#Tetris> .";
        Validation.validate(test);
    }
    public static void testBad()
    {
        String test = "";
        test += "<https://test> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/AIRO#AISystem> .";
        test += "<https://test> <https://w3id.org/AIRO#hasPurpose> <https://w3id.org/AIRO#GlobalWar> .";
        Validation.validate(test);
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
        nt += "<https://test> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/AIRO#AISystem> .";

        try{
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);
            String purpose = (String) jsonObject.get("purpose");        
            nt += "<https://test> <https://w3id.org/AIRO#hasPurpose> <https://w3id.org/AIRO#"+ purpose + "> .";
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
            System.out.println(conforms);
            /*if (!conforms) {
                String report = path.toFile().getAbsolutePath() + "/src/main/resources/report.ttl";
                File reportFile = new File(report);
                reportFile.createNewFile();
                OutputStream reportOutputStream = new FileOutputStream(reportFile);
                RDFDataMgr.write(reportOutputStream, reportResource.getModel(), RDFFormat.TURTLE);
            }*/
            return conforms;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
}
