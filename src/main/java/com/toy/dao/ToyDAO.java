package com.toy.dao;

import com.toy.model.Toy;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ToyDAO {
    private static final Logger LOGGER = Logger.getLogger(ToyDAO.class.getName());
    private File file;
    private static final String DATA_FILE = "E:/Personal Projects/Online Toy Store/Toy Inventory Management/src/main/webapp/database/toys.txt";

    public ToyDAO() {
        try {
            // Use the absolute path for the data file
            file = new File(DATA_FILE);
            LOGGER.info("Data file path: " + file.getAbsolutePath());
            
            // Create parent directories if they don't exist
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
                LOGGER.info("Created parent directory: " + parentDir.getAbsolutePath());
            }
            
            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
                LOGGER.info("Created new data file");
            }

            // Verify file is readable and writable
            if (!file.canRead()) {
                LOGGER.severe("Data file is not readable: " + file.getAbsolutePath());
            }
            if (!file.canWrite()) {
                LOGGER.severe("Data file is not writable: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initializing ToyDAO", e);
        }
    }

    public void addToy(Toy toy) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(toy.toString());
            LOGGER.info("Added toy: " + toy.getName() + " with image: " + toy.getImageName());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error adding toy", e);
        }
    }

    public List<Toy> getAllToys() {
        List<Toy> toys = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                LOGGER.info("Reading line " + lineNumber + ": " + line);
                String[] parts = line.split(",");
                if (parts.length == 4) {  // Updated to match new format
                    try {
                        Toy toy = new Toy(
                            parts[0], // imageName
                            parts[1], // name
                            parts[2], // description
                            Double.parseDouble(parts[3]) // price
                        );
                        toys.add(toy);
                        LOGGER.info("Successfully parsed toy: " + toy.getName());
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid price format in line " + lineNumber + ": " + parts[3]);
                    }
                } else {
                    LOGGER.warning("Invalid line format in line " + lineNumber + ": " + line);
                }
            }
            LOGGER.info("Retrieved " + toys.size() + " toys");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading toys", e);
        }
        return toys;
    }

    public Toy getToyByImageName(String imageName) {
        List<Toy> toys = getAllToys();
        for (Toy toy : toys) {
            if (toy.getImageName().equals(imageName)) {
                return toy;
            }
        }
        return null;
    }

    public void updateToy(Toy updatedToy) {
        List<Toy> toys = getAllToys();
        for (int i = 0; i < toys.size(); i++) {
            if (toys.get(i).getImageName().equals(updatedToy.getImageName())) {
                toys.set(i, updatedToy);
                break;
            }
        }
        saveAllToys(toys);
        LOGGER.info("Updated toy: " + updatedToy.getName());
    }

    public void deleteToy(String imageName) {
        List<Toy> toys = getAllToys();
        for (int i = 0; i < toys.size(); i++) {
            if (toys.get(i).getImageName().equals(imageName)) {
                toys.remove(i);
                break;
            }
        }
        saveAllToys(toys);
        LOGGER.info("Deleted toy with image name: " + imageName);
    }

    private void saveAllToys(List<Toy> toys) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            for (Toy toy : toys) {
                out.println(toy.toString());
            }
            LOGGER.info("Saved " + toys.size() + " toys to file");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving toys", e);
        }
    }

    public List<Toy> searchToys(String query) {
        List<Toy> toys = getAllToys();
        List<Toy> results = new LinkedList<>();
        query = query.toLowerCase();
        
        for (Toy toy : toys) {
            if (toy.getName().toLowerCase().contains(query) ||
                toy.getDescription().toLowerCase().contains(query)) {
                results.add(toy);
            }
        }
        LOGGER.info("Search for '" + query + "' returned " + results.size() + " results");
        return results;
    }
} 