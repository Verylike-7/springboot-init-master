# PDF Reference Extractor

Java command line tool for reading all PDF files in a directory and extracting references from the "引用文件" or "规范性引用文件" section.

## Build

```bash
mvn package
```

## Run

```bash
java -jar target/pdf-reference-extractor-1.0.0.jar /path/to/pdf-directory
```

If the directory contains one PDF, the output is a JSON object. If it contains multiple PDFs, the output is a JSON array.
