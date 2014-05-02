/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sdftosmiles;

import com.javamex.classmexer.MemoryUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.openscience.cdk.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.*;
import org.openscience.cdk.io.random.RandomAccessSDFReader;
import java.util.ArrayList;
import java.util.List;

import java.lang.instrument.Instrumentation;  
  
/**
 *
 * @author Palpatine
 */
public class SDFtoSMILES {
    static String input = "../../SMARTS/db/chembl_full1.sdf";
    static String output = "../../SMARTS/db/chembl_full1.sml";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, CDKException {
        BufferedOutputStream writer =  new BufferedOutputStream(new FileOutputStream(output));
        int j = 0;
        
        File f;
        
        SmilesGenerator sg;
        f = new File(input);

        System.out.println(input);
        IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(f), DefaultChemObjectBuilder.getInstance());

        sg = SmilesGenerator.generic().aromatic();
        sg.setUseAromaticityFlag(true);
        
        List<IAtomContainer> mols = new ArrayList<IAtomContainer>();

        IAtomContainer mol;
        
        long totalMemory = 0;

        while(reader.hasNext()){
            mol = (IAtomContainer)reader.next();
            mols.add(mol);
            j++;
            String smiles = sg.create(mol);
            if(!smiles.equals("")){
                writer.write(sg.create(mol).getBytes());
                writer.write(";".getBytes());
                writer.write(((String)mol.getProperties().get("chembl_id")).getBytes());
                writer.write("\n".getBytes());
            }
            else{
                System.out.println("HUUUUGE");
            }     
            System.out.println("done" + j);
        }
        
        totalMemory = MemoryUtil.deepMemoryUsageOfAll(mols, MemoryUtil.VisibilityFilter.ALL);
        
        System.out.println("Average memory: " + (totalMemory/j));
        
        /*a a = new a();
        a aa = new a();
        a.c = new c();
        aa.c = a.c;
        
        System.out.println("a " + MemoryUtil.deepMemoryUsageOf(a, MemoryUtil.VisibilityFilter.ALL));
        System.out.println("aa " + MemoryUtil.deepMemoryUsageOf(aa, MemoryUtil.VisibilityFilter.ALL));
        System.out.println("a.c " + MemoryUtil.deepMemoryUsageOf(a.c, MemoryUtil.VisibilityFilter.ALL));
        
        List<a> ll = new ArrayList<a>();
        
        ll.add(a);
        ll.add(aa);
        
        System.out.println(MemoryUtil.deepMemoryUsageOfAll(ll, MemoryUtil.VisibilityFilter.ALL));*/
        
        
        System.out.println("done" + j);
        writer.close();
    }
}


class a{
    int a = 32;
    int b = 64;
    public c c;
}

class c{
    int a = 55;
    int b = 69;
    int c = 66;
    String x = "asdasdasdasdasd";
}