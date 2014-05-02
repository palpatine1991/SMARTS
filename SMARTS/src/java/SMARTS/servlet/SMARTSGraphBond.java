/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.matchers.IQueryBond;
import org.openscience.cdk.isomorphism.matchers.smarts.AromaticQueryBond;
import org.openscience.cdk.isomorphism.matchers.smarts.LogicalOperatorBond;
import org.openscience.cdk.isomorphism.matchers.smarts.OrderQueryBond;


/**
 *
 * @author Palpatine
 */
public class SMARTSGraphBond {
    public String firstAtomId;
    public String secondAtomId;
    public SMARTSGraphAtom firstAtom;
    public SMARTSGraphAtom secondAtom;
    boolean singleBond = false;
    boolean doubleBond = false;
    boolean tripleBond = false;
    boolean aromaticBond = false;
    int typeCount = 0;
}
