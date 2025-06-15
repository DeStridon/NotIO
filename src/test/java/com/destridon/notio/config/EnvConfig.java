package com.destridon.notio.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class EnvConfig {
    private static final Dotenv dotenv;
    
    static {
        dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    }
    
    public static String getEnv(String key) {
        String value = dotenv.get(key);
        if (value == null || value.trim().isEmpty()) {
            System.out.println(key + " not found in .env file.");
            System.out.print("Please enter your " + key + ": ");
            
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                value = reader.readLine().trim();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read input: " + e.getMessage(), e);
            }
            
            // Save to .env file
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get(".env"),(key + "=" + value + "\n").getBytes(),java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
            } catch (Exception e) {
                System.err.println("Warning: Could not save " + key + " to .env file: " + e.getMessage());
            }
        }
        return value;
    }
} 
