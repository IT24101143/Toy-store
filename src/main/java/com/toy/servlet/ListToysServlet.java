package com.toy.servlet;

import com.toy.dao.ToyDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;

@WebServlet("/list-toys")
public class ListToysServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ListToysServlet.class.getName());
    private ToyDAO toyDAO;
    private static final String LIST_TOYS_JSP = "/list-toys.jsp";
    private static final String IMAGES_DIRECTORY = "E:/Personal Projects/Online Toy Store/Toy Inventory Management/src/main/webapp/images";

    @Override
    public void init() {
        toyDAO = new ToyDAO();
        LOGGER.info("ListToysServlet initialized");
        
        // Verify images directory exists
        File imagesDir = new File(IMAGES_DIRECTORY);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
            LOGGER.info("Created images directory: " + imagesDir.getAbsolutePath());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LOGGER.info("Processing list-toys request");
            String searchQuery = request.getParameter("search");
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                LOGGER.info("Searching for toys with query: " + searchQuery);
                request.setAttribute("toys", toyDAO.searchToys(searchQuery));
            } else {
                LOGGER.info("Getting all toys");
                request.setAttribute("toys", toyDAO.getAllToys());
            }
            
            // Set the images directory path for the JSP
            request.setAttribute("imagesPath", IMAGES_DIRECTORY);
            
            LOGGER.info("Forwarding to " + LIST_TOYS_JSP);
            request.getRequestDispatcher(LIST_TOYS_JSP).forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in ListToysServlet", e);
            throw e;
        }
    }
} 