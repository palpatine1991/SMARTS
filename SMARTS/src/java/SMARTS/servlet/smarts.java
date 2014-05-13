/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;


public class smarts extends HttpServlet {
    
    String PATH;;
    List<DbRecord> db = new ArrayList<DbRecord>();;
    Map<String,byte[]> filters = new HashMap<String,byte[]>();
    Map<String,List<Integer>> smallFilters = new HashMap<String,List<Integer>>();
    Map<String, Integer> filterStats = new HashMap<String, Integer>();
    int numberOfRecords;
    int listLimit;
    
    public void init(ServletConfig config){
        PATH = config.getInitParameter("dbPath");
        try {
            getIndexInfoToMemory();
            getFiltersToMemory(); 
            getDatabaseToMemory();
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
        
        HttpSession session = request.getSession();   
        
        int valid = 0;
        ProgressObject progress = null;
        int i = 0;
        int oldValid = 0;
        byte[] filterByte;
        List<Integer> filter;
        
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
            
            progress = new ProgressObject(numberOfRecords);

            session.setAttribute("progress", progress);

            SMARTSGraph graph = getSmartsGraph(json);
            filterByte = graph.getFilter();
            
            filter = byteToList(filterByte);

            session.setAttribute("filter", filter);

            i = 0;
            valid = 0;
            session.setAttribute("valid", valid);
           
        }
        else{
            System.out.println("TimeStamp not null");
            filter = (List<Integer>)session.getAttribute("filter");
            valid = Integer.parseInt(session.getAttribute("valid").toString());
            oldValid = valid;
            progress = (ProgressObject)session.getAttribute("progress");
            
            i = progress.progress;
        }
        sb.append("[");
        
        DbRecord record;
        boolean match;
        System.out.println("filterSize: " + filter.size());
        long startTime = System.currentTimeMillis(); 
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol;
        //for(int j = i; j < numberOfRecords; j++){
        for(int j : filter){
            //Prepsat for cyklus na foreach pres filter
            if(j <= i){
                continue;
            }
            record = db.get(j - 1);
            progress.progress = j;
            try{
                mol = sp.parseSmiles(record.smiles);
                match = querytool.matches(mol);

                if(match){
                    valid++;
                    session.setAttribute("valid", valid);
                    sb.append(",{\"link\" : \"").append(record.url).append("\",");
                    sb.append("\"valid_number\" : ").append(valid).append(",");
                    sb.append("\"id\" : ").append(j).append(",");
                    sb.append("\"smiles\" : \"").append(sg.create(mol)).append("\"}");
                    if(valid - oldValid == numOfRecords){
                        break;
                    }
                }
            }
            catch(CDKException ex){
                System.out.println("huge structure");
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
        String path = PATH + "index/";
        
        for(int i = 1; i <= 109; i++){
            addFilter(Integer.toString(i), path);
        }
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){     
                System.out.println("reading filters: " + i + " , " + j);
                addFilter(i + "-" + j, path);
                addFilter(i + "=" + j, path);
                addFilter(i + "#" + j, path);
                addFilter(i + "~" + j, path);
            }
            for(int j = 0; j <= 10; j++){
                addFilter(i + ";v" + j,path);
            }
            for(int j = -10; j <= 10; j++){
                if(j >=0){
                    addFilter(i + ";+" + j,path);
                }
                else{
                    addFilter(i + ";" + j,path);
                }
            }
        }
    }
    
    private void addFilter(String fileName, String path) throws IOException{
        File f = new File(path + fileName + ".index");
        
        if(f.exists() && !f.isDirectory() && filterStats.get(fileName) >= listLimit) {
            filters.put(fileName, Files.readAllBytes(Paths.get(path + fileName + ".index")));
        }
        else if(f.exists() && !f.isDirectory()){
            byte[] tmpArray = Files.readAllBytes(Paths.get(path + fileName + ".index"));
            smallFilters.put(fileName, byteToList(tmpArray));
        }
        else{
            List<Integer> tmp = new ArrayList<Integer>();
            smallFilters.put(fileName, tmp);
        }
    }
    
    private void getIndexInfoToMemory() throws FileNotFoundException, IOException{
        BufferedReader indexInfo = new BufferedReader(new FileReader(PATH + "index/info.index"));
        String firstLine = indexInfo.readLine();
        numberOfRecords = Integer.parseInt(firstLine.split(";;")[1]);
        listLimit = numberOfRecords / 32;
        String[] splitLine;
        
        String line = indexInfo.readLine();
        while(line != null){
            splitLine = line.split(";;");
            filterStats.put(splitLine[0], Integer.parseInt(splitLine[1]));
            line = indexInfo.readLine();
        }
    }
    
    private void getDatabaseToMemory() throws FileNotFoundException, IOException, InvalidSmilesException{
        BufferedReader br = new BufferedReader(new FileReader(PATH + "chembl_full.sml"));
        String line = br.readLine();
        String[] splitLine;
        IAtomContainer mol;
        int i = 0;
        
        while(line != null){
            splitLine = line.split(";");
           
            db.add(new DbRecord(splitLine[1], splitLine[0]));
            i++;
            System.out.println(i);
            line = br.readLine();
        }
    }
    
    private SMARTSGraph getSmartsGraph(String json) throws FileNotFoundException, UnsupportedEncodingException{

        SMARTSGraph graph = new Gson().fromJson(json, SMARTSGraph.class);
        
        graph.filters = filters;
        graph.smallFilters = smallFilters;
        graph.filterStats = filterStats;
        graph.numberOfRecords = numberOfRecords;
        graph.listLimit = listLimit;
        
        for(String key : graph.atoms.keySet()){
            SMARTSGraphAtom atom = graph.atoms.get(key);
            atom.bonds = new ArrayList<SMARTSGraphBond>();
            graph.atoms.put(key, atom);
        }
        
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
    
    private List<Integer> byteToList(byte[] filter) throws IOException{
        List<Integer> result = new ArrayList<Integer>();
        
        for(int i = 0; i < filter.length; i++){
            if(filter[i] == 0){
                continue;
            }
            for(int j = 7; j >=0; j--){
                if((filter[i] >> j) % 2 != 0){
                    result.add((i * 8) + (8 - j));
                }
            }
        }
        
        return result;
    }
}

class DbRecord{
    String url;
    String smiles;
    
    public DbRecord(String url, String smiles){
        this.url = url;
        this.smiles = smiles;
    }
}
