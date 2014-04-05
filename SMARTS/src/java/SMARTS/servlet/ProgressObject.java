/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

/**
 *
 * @author Palpatine
 */
public class ProgressObject {
    public int progress;
    public int numberOfRecords;

    public ProgressObject(int numberOfRecords) {
        this.progress = 0;
        this.numberOfRecords = numberOfRecords;
    }
}
