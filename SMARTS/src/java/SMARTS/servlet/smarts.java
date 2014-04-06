/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openscience.cdk.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.smiles.smarts.*;
import org.openscience.cdk.smiles.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import org.openscience.cdk.ringsearch.AllRingsFinder;
//import org.openscience.cdk.smsd.global.TimeOut;


public class smarts extends HttpServlet {
    
    String PATH = "C:/bakalarka/server/SMARTS/db/";
    List<dbRecord> db = new ArrayList<dbRecord>();;
    Map<String,byte[]> filters = new HashMap<String,byte[]>();
    int numberOfRecords;
    
    public void init(ServletConfig config){
        try {
            System.out.println("Free Memory before init: " + Runtime.getRuntime().freeMemory() + " ; Total Memory before init: " + Runtime.getRuntime().totalMemory());


            getFiltersToMemory();
            getIndexInfoToMemory();
            getDatabaseToMemory();
            
            System.out.println("Free Memory after init: " + Runtime.getRuntime().freeMemory() + " ; Total Memory after init: " + Runtime.getRuntime().totalMemory());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(smarts.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(smarts.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidSmilesException ex) {
            Logger.getLogger(smarts.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("START");
        PrintWriter out = response.getWriter();
        long startTime = System.currentTimeMillis();
        HttpSession session = request.getSession();   
        
        boolean filtered = false;
        List<Integer> filter = null;
        int valid = 0;
        ProgressObject progress = null;
        int i = 0;
        int oldValid = 0;
        
        int actualProgress = 0;
        int numOfRecords = Integer.parseInt(request.getParameter("numOfRecords"));
        String s = request.getParameter("smarts");
        String json = request.getParameter("json");
        StringBuilder sb = new StringBuilder();
        SmilesGenerator sg = new SmilesGenerator();
        sg.setUseAromaticityFlag(true);
        System.out.println(s);
        SMARTSQueryTool querytool = new SMARTSQueryTool(s, DefaultChemObjectBuilder.getInstance());
        
        String tmpTimeStamp = null;
        
        if(session.getAttribute("timeStamp") != null){
            tmpTimeStamp = session.getAttribute("timeStamp").toString();
        }
        
        
        if((tmpTimeStamp == null) || !(tmpTimeStamp.equals(request.getParameter("timeStamp")))){
            System.out.println("TimeStamp null");
            long timeStamp = Long.parseLong(request.getParameter("timeStamp"));
            
            session.setAttribute("timeStamp", timeStamp);

            SMARTSGraph graph = getSmartsGraph(json);
            Set<String> filterSet = graph.getIndexFileNames();
            filtered = false;
            if(filterSet.size() > 0){
                filtered = true;
            }
            
            session.setAttribute("filtered", filtered);


            progress = new ProgressObject(numberOfRecords);

            session.setAttribute("progress", progress);

            filter = null;

            if(filtered){
                filter = getFilter(filterSet);
            }
            session.setAttribute("filter", filter);

            i = 0;
            valid = 0;
            session.setAttribute("valid", valid);
           
        }
        else{
            System.out.println("TimeStamp not null");
            filtered = Boolean.parseBoolean(session.getAttribute("filtered").toString());
            if(filtered){
                filter = (List<Integer>)session.getAttribute("filter");
            }
            valid = Integer.parseInt(session.getAttribute("valid").toString());
            oldValid = valid;
            progress = (ProgressObject)session.getAttribute("progress");
            
            i = progress.progress;
        }
        sb.append("[");
        
        dbRecord record;
        boolean match;
        for(int j = i; j < numberOfRecords; j++){

            i++;
            System.out.println(i);
            record = db.get(j);
            progress.progress = i;
            if((!filtered) || (valid < filter.size() && filter.contains(i))){
                try{
                    match = querytool.matches(record.mol);

                    if(match){
                        valid++;
                        session.setAttribute("valid", valid);
                        sb.append(",{\"link\" : \"https://www.ebi.ac.uk/chembl/compound/inspect/" + record.id + "\",");
                        sb.append("\"valid_number\" : " + valid + ",");
                        sb.append("\"id\" : " + i + ",");
                        sb.append("\"smiles\" : \"" + record.smiles + "\"}");
                    }
                }
                catch(CDKException ex){
                    System.out.println("huge structure");
                }
            }
            if(valid - oldValid == numOfRecords){
                break;
            }
        }
        sb.append("]");
        if(sb.length() > 2){
            sb.deleteCharAt(1); //deleting starting comma
        }
        out.println(sb.toString());

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime);        
    }
    
    private void getFiltersToMemory() throws IOException{
        String path = PATH + "small_index/";
        
        for(int i = 1; i <= 109; i++){
            filters.put(Integer.toString(i), Files.readAllBytes(Paths.get(path + i + ".index")));
        }
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){     
                System.out.println("reading filters: " + i + " , " + j);
                filters.put(i + "-" + j, Files.readAllBytes(Paths.get(path + i + "-" + j + ".index")));
                filters.put(i + "=" + j, Files.readAllBytes(Paths.get(path + i + "=" + j + ".index")));
                filters.put(i + "#" + j, Files.readAllBytes(Paths.get(path + i + "#" + j + ".index")));
                filters.put(i + "~" + j, Files.readAllBytes(Paths.get(path + i + "~" + j + ".index")));
            }
        }
    }
    
    private void getIndexInfoToMemory() throws FileNotFoundException, IOException{
        BufferedReader indexInfo = new BufferedReader(new FileReader(PATH + "small_index/info.index"));
        String firstLine = indexInfo.readLine();
        numberOfRecords = Integer.parseInt(firstLine.split(";")[1]);
    }
    
    private void getDatabaseToMemory() throws FileNotFoundException, IOException, InvalidSmilesException{
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        BufferedReader br = new BufferedReader(new FileReader(PATH + "chembl_full1.sml"));
        String line = br.readLine();
        String[] splitLine;
        IAtomContainer mol;
        int i = 0;
        
        while(line != null){
            splitLine = line.split(";");
            mol = sp.parseSmiles(splitLine[0]);
            db.add(new dbRecord(splitLine[1], splitLine[0] ,mol));
            i++;
            System.out.println(i);
            line = br.readLine();
        }
    }
    
    private SMARTSGraph getSmartsGraph(String json) throws FileNotFoundException, UnsupportedEncodingException{

        SMARTSGraph graph = new Gson().fromJson(json, SMARTSGraph.class);
        
        for(SMARTSGraphBond bond : graph.bonds){
            bond.firstAtom = graph.atoms.get(bond.firstAtomId);
            bond.secondAtom = graph.atoms.get(bond.secondAtomId);
            
            bond.firstAtom.bonds.add(bond);
            bond.secondAtom.bonds.add(bond);
            
            int counter = 0;
            if(bond.singleBond){
                counter++;
            }
            if(bond.doubleBond){
                counter++;
            }
            if(bond.tripleBond){
                counter++;
            }
            if(bond.aromaticBond){
                counter++;
            }
            
            bond.typeCount = counter;
        }
        
        
        
        return graph;
    }
    
    //gets unempty set
    private List<Integer> getFilter(Set<String> set) throws IOException{
        List<Integer> result = new ArrayList<Integer>();
        
        if(set.size() == 0){
            return result;
        }
        
        byte[] actualBitstring = null;
        boolean firstRecord = true;
        
        for(String fileName : set){
            if(firstRecord){
                actualBitstring = filters.get(fileName);//Files.readAllBytes(Paths.get(PATH + "small_index/" + fileName + ".index"));
                firstRecord = false;
            }
            else{
                actualBitstring = doAnd(actualBitstring, filters.get(fileName)/*Files.readAllBytes(Paths.get(PATH + "small_index/" + fileName + ".index"))*/);
            }        
        }
        for(int i = 0; i < actualBitstring.length; i++){
            for(int j = 7; j >=0; j--){
                if((actualBitstring[i] >> j) % 2 != 0){
                    result.add((i * 8) + (8 - j));
                }
            }
        }
        
        return result;
    }
    
    private byte[] doAnd(byte[] arr1, byte[] arr2){
        for(int i = 0; i < arr1.length; i++){
            arr1[i] = (byte)(arr1[i] & arr2[i]);
        }
        return arr1;
    }
}

class dbRecord{
    String id;
    IAtomContainer mol;
    String smiles;
    
    public dbRecord(String id, String smiles, IAtomContainer mol){
        this.id = id;
        this.smiles = smiles;
        this.mol = mol;
    }
}
