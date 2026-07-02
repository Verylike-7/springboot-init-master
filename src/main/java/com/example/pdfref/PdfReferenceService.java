package com.example.pdfref;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public final class PdfReferenceService {
    public List<FileReferenceResult> extractFromDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Path is not a directory: " + directory);
        }

        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(PdfReferenceService::isPdf)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::extractFromPdf)
                    .toList();
        }
    }

    private FileReferenceResult extractFromPdf(Path pdfPath) {
        try {
            String text = readPdfText(pdfPath);
            return new FileReferenceResult(
                    pdfPath.getFileName().toString(),
                    ReferenceExtractor.extract(text));
        } catch (IOException e) {
            throw new PdfReadException("Failed to read PDF: " + pdfPath, e);
        }
    }

    private static String readPdfText(Path pdfPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static boolean isPdf(Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".pdf");
    }
}
