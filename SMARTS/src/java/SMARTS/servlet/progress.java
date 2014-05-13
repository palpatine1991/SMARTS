/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Palpatine
 */
@WebServlet(name = "progress", urlPatterns = {"/progress"})
public class progress extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            PrintWriter out = response.getWriter();
            
            HttpSession session = request.getSession();
            ProgressObject progress = (ProgressObject)session.getAttribute("progress");
            if(progress != null){
                out.println("progress: " + progress.progress + " / " + progress.numberOfRecords);
            }
            else{
                out.println("progress: Initializing...");
            }
            
    }
}
