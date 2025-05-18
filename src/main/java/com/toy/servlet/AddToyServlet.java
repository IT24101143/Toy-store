package com.toy.servlet;

import com.toy.dao.ToyDAO;
import com.toy.model.Toy;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/add-toy")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 5,   // 5 MB
    maxRequestSize = 1024 * 1024 * 10 // 10 MB
)
public class AddToyServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AddToyServlet.class.getName());
    private static final String ADD_TOY_JSP = "/add-toy.jsp";
    private ToyDAO toyDAO;

    @Override
    public void init() {
        toyDAO = new ToyDAO();
        // Create upload directory if it doesn't exist
        String uploadPath = getServletContext().getRealPath("/images");
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            LOGGER.info("Created upload directory: " + uploadDir.getAbsolutePath());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher(ADD_TOY_JSP).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Get form data
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            double price = Double.parseDouble(request.getParameter("price"));

            // Handle image upload
            Part filePart = request.getPart("image");
            String fileName = getSubmittedFileName(filePart);
            
            if (fileName != null && !fileName.isEmpty()) {
                // Get the file extension
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                // Generate a unique filename using timestamp
                String uniqueFileName = System.currentTimeMillis() + fileExtension;
                
                // Save the file
                String uploadPath = getServletContext().getRealPath("") + File.separator + "images";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                
                filePart.write(uploadPath + File.separator + uniqueFileName);
                
                LOGGER.info("Image uploaded successfully: " + uniqueFileName);

                // Create and save the toy
                Toy toy = new Toy(uniqueFileName, name, description, price);
                toyDAO.addToy(toy);
                
                response.sendRedirect(request.getContextPath() + "/list-toys");
            } else {
                throw new ServletException("No image file was uploaded");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding toy", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding toy: " + e.getMessage());
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
} 