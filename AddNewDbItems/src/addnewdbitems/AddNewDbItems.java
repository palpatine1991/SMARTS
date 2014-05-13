/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addnewdbitems;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import indexlibrary.IndexLibrary;

/**
 *
 * @author Palpatine
 */
public class AddNewDbItems {

    static String path = "C:/bakalarka/server/SMARTS/db/";
    static String db = "chembl_test_small.sml";
    static String newDb = "chembl_test_small1.sml";
    static int newDbSize = 0;
    static String index = "tiny_index";
    static Map<String, Integer> filterStats = new HashMap<String, Integer>();
    static int numberOfRecords;
    static int byteOverlap;
    static BufferedReader br;
    static SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    static PrintWriter info;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, CDKException {
        getIndexInfoToMemory();
        
        
        info = new PrintWriter(path + index + "/info.index", "UTF-8");
        joinDBs();
        makeSimpleIndex();
        makePairIndex(); 
        makeChargeIndex();
        makeValenceIndex();
        info.close();
        
    }
    
    private static void joinDBs(){
        try
        {
            FileWriter fw = new FileWriter(path + db,true);
            BufferedWriter bufferWritter = new BufferedWriter(fw);
            br = new BufferedReader(new FileReader(path + newDb));
            String line = br.readLine();
            while(line != null){
                bufferWritter.write(line);
                bufferWritter.newLine();
                newDbSize++;
                line = br.readLine();
            }
    	    bufferWritter.close();
            fw.close();
            
            int newSize = numberOfRecords + newDbSize;
            
            info.println("numberOfRecords;;" + newSize);
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
    
    private static void getIndexInfoToMemory() throws FileNotFoundException, IOException{
        BufferedReader indexInfo = new BufferedReader(new FileReader(path + index + "/info.index"));
        String firstLine = indexInfo.readLine();
        numberOfRecords = Integer.parseInt(firstLine.split(";;")[1]);
        String[] splitLine;
        
        String line = indexInfo.readLine();
        while(line != null){
            splitLine = line.split(";;");
            filterStats.put(splitLine[0], Integer.parseInt(splitLine[1]));
            line = indexInfo.readLine();
        }
        
        byteOverlap = numberOfRecords % 8;
    }
    
    private static void makeSimpleIndex() throws FileNotFoundException, CDKException, IOException{
       
        BufferedOutputStream[] outs = new BufferedOutputStream[109];
        int[] results = new int[109];
        int[] matches = new int[109];
        boolean[] matchStructure = new boolean[109];
        File f;
        for(int i = 0; i < 109; i++){
            f = new File(path + index + "/" + (i + 1) + ".index");
            if(byteOverlap == 0){
                if(f.exists() && !f.isDirectory()) {
                    outs[i] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ".index", true));
                }
                else{
                    outs[i] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ".index"));
                    int numOfBytes = numberOfRecords / 8;
                    for(int j = 0; j < numOfBytes; j++){
                        outs[i].write(0);
                    }
                }               
                results[i] = 0;
            }
            else{                
                if(f.exists() && !f.isDirectory()) {
                    byte[] tmpArray = Files.readAllBytes(Paths.get(path + index + "/" + (i + 1) + ".index"));
                    byte[] tmpSmallerArray = new byte[tmpArray.length - 1];
                    for(int j = 0; j < tmpSmallerArray.length; j++){
                        tmpSmallerArray[j] = tmpArray[j];
                    }
                    outs[i] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ".index"));
                    outs[i].write(tmpSmallerArray);
                    results[i] = tmpArray[tmpArray.length - 1] >> (8 - byteOverlap);
                }
                else{
                    outs[i] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ".index"));
                    int numOfBytes = (numberOfRecords / 8) - 1;
                    for(int j = 0; j < numOfBytes; j++){
                        outs[i].write(0);
                    }
                    results[i] = 0;
                }
            }
            matches[i] = filterStats.get(Integer.toString(i + 1));
        }
        br = new BufferedReader(new FileReader(path + newDb));
 
        int bit = byteOverlap;
        int[] tmpArray = new int[1];
        bit = IndexLibrary.createSimpleIndex(bit, br, tmpArray, matchStructure, matches, results, outs);
        IndexLibrary.closeByte(bit, results, outs);
         
        for(int i = 1; i <= 109; i++){
            info.println(i + ";;" + matches[i - 1]);
            outs[i - 1].close();
        }
        for(int i = 1; i <= 109; i++){
            if(matches[i - 1] == 0){
                f = new File(path + index + "/" + i + ".index");
                f.delete();
            }
        }
        br.close();   
    }
    
    private static void makePairIndex() throws FileNotFoundException, IOException, InvalidSmilesException{
        
        Map<String, Boolean> matchStructure = new HashMap<String, Boolean>();
        Map<String, Integer> numOfMatches = new HashMap<String, Integer>();
        Map<String, Integer> results = new HashMap<String, Integer>();
        Map<String, BufferedOutputStream> outs = new HashMap<String, BufferedOutputStream>();
        
        generateAllPairsBool(matchStructure);
        generateAllPairsMatches(numOfMatches);
        generateAllPairsStream(outs, results);
        
        br = new BufferedReader(new FileReader(path + newDb));
 
        int bit = byteOverlap;
        int i = 0;
        bit = IndexLibrary.createPairIndex(bit, br, matchStructure, numOfMatches, results, outs);
        IndexLibrary.closeByte(bit, results, outs);
        for(String key : outs.keySet()){
           outs.get(key).close();
        }
        for(String key : numOfMatches.keySet()){
            if(numOfMatches.get(key) == 0){
                File f = new File(path + index + "/" + key + ".index");
                f.delete();
            }
            info.println(key + ";;" + numOfMatches.get(key));
        }
        br.close();
    }
    
    private static void generateAllPairsBool(Map<String, Boolean> map){
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){
                map.put(i + "-" + j, false);
                map.put(i + "=" + j, false);
                map.put(i + "#" + j, false);
                map.put(i + "~" + j, false);
            }
        }
    }
    
    private static void generateAllPairsMatches(Map<String, Integer> map){
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){
                map.put(i + "-" + j, filterStats.get(i + "-" + j));
                map.put(i + "=" + j, filterStats.get(i + "=" + j));
                map.put(i + "#" + j, filterStats.get(i + "#" + j));
                map.put(i + "~" + j, filterStats.get(i + "~" + j));
            }
        }
    }
    
    private static void generateAllPairsStream(Map<String, BufferedOutputStream> map, Map<String, Integer> results) throws FileNotFoundException, IOException{
        Map<String, File> files ;
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){
                files = new HashMap<String, File>();
                files.put(i + "-" + j, new File(path + index + "/" + i + "-" + j + ".index"));
                files.put(i + "=" + j, new File(path + index + "/" + i + "=" + j + ".index"));
                files.put(i + "#" + j, new File(path + index + "/" + i + "#" + j + ".index"));
                files.put(i + "~" + j, new File(path + index + "/" + i + "~" + j + ".index"));
                if(byteOverlap == 0){
                    for(String s : files.keySet()){
                        File f = files.get(s);
                        results.put(s, 0);
                        if(f.exists() && !f.isDirectory()) {
                            map.put(s, new BufferedOutputStream(new FileOutputStream(path + index + "/" + s + ".index", true)));
                        }
                        else{
                            map.put(s, new BufferedOutputStream(new FileOutputStream(path + index + "/" + s + ".index")));
                            int numOfBytes = numberOfRecords / 8;
                            for(int k = 0; k < numOfBytes; k++){
                                map.get(s).write(0);
                            }
                        }               
                        results.put(s, 0);
                    }
                }
                else{    
                    for(String s : files.keySet()){
                        File f = files.get(s);
                        if(f.exists() && !f.isDirectory()) {
                            byte[] tmpArray = Files.readAllBytes(Paths.get(path + index + "/" + s + ".index"));
                            byte[] tmpSmallerArray = new byte[tmpArray.length - 1];
                            for(int k = 0; k < tmpSmallerArray.length; k++){
                                tmpSmallerArray[k] = tmpArray[k];
                            }
                            map.put(s, new BufferedOutputStream(new FileOutputStream(path + index + "/" + s + ".index")));
                            map.get(s).write(tmpSmallerArray);
                            results.put(s, tmpArray[tmpArray.length - 1] >> (8 - byteOverlap));
                        }
                        else{
                            map.put(s, new BufferedOutputStream(new FileOutputStream(path + index + "/" + s + ".index")));
                            int numOfBytes = (numberOfRecords / 8) - 1;
                            for(int k = 0; k < numOfBytes; k++){
                                map.get(s).write(0);
                            }
                            results.put(s, 0);
                        }
                    }
                }
            }
        }
    }
    
    private static void makeChargeIndex() throws FileNotFoundException, IOException, InvalidSmilesException, CDKException {
        BufferedOutputStream[][] outs = new BufferedOutputStream[109][21]; //charge = index -10
        int[][] results = new int[109][21];
        int[][] matches = new int[109][21];
        boolean[][] matchStructure = new boolean[109][21];
        File f;
        for(int i = 0; i < 109; i++){
            for(int j = 0; j < 21; j++){
                String fileName;
                if(j < 10){
                    fileName = (i + 1) + ";" + (j - 10);
                }
                else{
                    fileName = (i + 1) + ";+" + (j - 10);
                }
                f = new File(path + index + "/" + fileName + ".index");
                if(byteOverlap == 0){
                    if(f.exists() && !f.isDirectory()) {
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index", true));
                    }
                    else{
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index"));
                        int numOfBytes = numberOfRecords / 8;
                        for(int k = 0; k < numOfBytes; k++){
                            outs[i][j].write(0);
                        }
                    }               
                    results[i][j] = 0;
                }
                else{                
                    if(f.exists() && !f.isDirectory()) {
                        byte[] tmpArray = Files.readAllBytes(Paths.get(path + index + "/" + fileName + ".index"));
                        byte[] tmpSmallerArray = new byte[tmpArray.length - 1];
                        for(int k = 0; k < tmpSmallerArray.length; k++){
                            tmpSmallerArray[k] = tmpArray[k];
                        }
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index"));
                        outs[i][j].write(tmpSmallerArray);
                        results[i][j] = tmpArray[tmpArray.length - 1] >> (8 - byteOverlap);
                    }
                    else{
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index"));
                        int numOfBytes = (numberOfRecords / 8) - 1;
                        for(int k = 0; k < numOfBytes; k++){
                            outs[i][j].write(0);
                        }
                        results[i][j] = 0;
                    }
                }
                matches[i][j] = filterStats.get(fileName);
            }
        }
        br = new BufferedReader(new FileReader(path + newDb));
 
        int bit = byteOverlap;
        bit = IndexLibrary.createChargeIndex(bit, br, matchStructure, matches, results, outs);
        IndexLibrary.closeByte(bit, results, outs, 21);

        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 21; j++){ 
               String fileName = i + ";";
               if(j >= 10){
                   fileName += "+";
               }
               fileName += (j - 10);
               info.println(fileName + ";;" + matches[i - 1][j]);
               outs[i - 1][j].close();
            }
        }
        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 21; j++){ 
               if(matches[i - 1][j] == 0){
                   String fileName = i + ";";
                   if(j >= 10){
                       fileName += "+";
                   }
                   fileName += (j - 10) + ".index";
                   f = new File(path + index + "/" + fileName);
                   f.delete();
               }
            }
        }
        br.close();
    }
    
    private static void makeValenceIndex() throws FileNotFoundException, IOException, InvalidSmilesException, CDKException {
        BufferedOutputStream[][] outs = new BufferedOutputStream[109][11];
        int[][] results = new int[109][11];
        int[][] matches = new int[109][11];
        boolean[][] matchStructure = new boolean[109][11];
        File f;
        for(int i = 0; i < 109; i++){
            for(int j = 0; j < 11; j++){
                String fileName = (i + 1) + ";v" + j;
                f = new File(path + index + "/" + fileName + ".index");
                if(byteOverlap == 0){
                    if(f.exists() && !f.isDirectory()) {
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index", true));
                    }
                    else{
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index"));
                        int numOfBytes = numberOfRecords / 8;
                        for(int k = 0; k < numOfBytes; k++){
                            outs[i][j].write(0);
                        }
                    }               
                    results[i][j] = 0;
                }
                else{                
                    if(f.exists() && !f.isDirectory()) {
                        byte[] tmpArray = Files.readAllBytes(Paths.get(path + index + "/" + fileName + ".index"));
                        byte[] tmpSmallerArray = new byte[tmpArray.length - 1];
                        for(int k = 0; k < tmpSmallerArray.length; k++){
                            tmpSmallerArray[k] = tmpArray[k];
                        }
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index"));
                        outs[i][j].write(tmpSmallerArray);
                        results[i][j] = tmpArray[tmpArray.length - 1] >> (8 - byteOverlap);
                    }
                    else{
                        outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + fileName + ".index"));
                        int numOfBytes = (numberOfRecords / 8) - 1;
                        for(int k = 0; k < numOfBytes; k++){
                            outs[i][j].write(0);
                        }
                        results[i][j] = 0;
                    }
                }
                matches[i][j] = filterStats.get(fileName);
            }
        }
        br = new BufferedReader(new FileReader(path + newDb));

        int bit = byteOverlap;
        bit = IndexLibrary.createValenceIndex(bit, br, matchStructure, matches, results, outs);
        IndexLibrary.closeByte(bit, results, outs, 11);
        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 11; j++){ 
               info.println(i + ";v" + j + ";;" + matches[i - 1][j]);
               outs[i - 1][j].close();
            }
        }
        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 11; j++){ 
               if(matches[i - 1][j] == 0){
                   String fileName = i + ";v" + j + ".index";
                   f = new File(path + index + "/" + fileName);
                   f.delete();
               }
            }
        }
        br.close();
    }
}
