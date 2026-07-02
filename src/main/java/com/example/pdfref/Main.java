package com.example.pdfref;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        suppressPdfLibraryLogs();

        if (args.length != 1) {
            System.err.println("Usage: java -jar pdf-reference-extractor-1.0.0.jar <pdf-directory>");
            System.exit(1);
        }

        List<FileReferenceResult> results = new PdfReferenceService().extractFromDirectory(Path.of(args[0]));
        Object output = results.size() == 1 ? results.get(0) : results;

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(mapper.writeValueAsString(output));
    }

    private static void suppressPdfLibraryLogs() {
        Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);
        Logger.getLogger("org.apache.fontbox").setLevel(Level.SEVERE);
    }
}
