/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indexbuilding;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.openscience.cdk.exception.CDKException;

/**
 *
 * @author Palpatine
 */
public class IndexBuilding {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, CDKException {
        new IndexBuilder();
    }
}