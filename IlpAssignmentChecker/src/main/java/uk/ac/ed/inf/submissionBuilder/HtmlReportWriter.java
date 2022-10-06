package uk.ac.ed.inf.submissionBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class HtmlReportWriter {

    private BufferedWriter writer;
    private Path reportFileName;
    private StringBuilder bodyContent = new StringBuilder();
    private String title;
    private String templateFileName;

    public HtmlReportWriter(Path reportFileName, String title, String templateFileName) throws IOException {
        this.title = title;
        this.reportFileName = reportFileName;
        this.templateFileName = templateFileName;
        writer = new BufferedWriter(new FileWriter(reportFileName.toFile()));
        bodyContent.append(String.format("<h1>%s</h1>", title));
    }

    public void beginSection(String sectionHeader, String sectionClassName) {
        bodyContent.append(String.format("<h3>%s</h3><div class='%s'>", sectionHeader, (sectionClassName != null ? sectionClassName : "")));
    }

    public void beginCodeSection(String sectionHeader, String sectionClassName) {
        bodyContent.append(String.format("<h3>%s</h3><pre><code class='%s'>", sectionHeader, (sectionClassName != null ? sectionClassName : "")));
    }

    public void endSection() {
        bodyContent.append("</div>");
    }

    public void endCodeSection() {
        bodyContent.append("</code></pre>");
    }

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }


    public void writeReport() throws IOException {
        InputStream template = getFileFromResourceAsStream(templateFileName);
        String htmlString = new String(template.readAllBytes(), StandardCharsets.UTF_8);
        htmlString = htmlString.replace("$title", title);
        htmlString = htmlString.replace("$body", bodyContent.toString());
        writer.write(htmlString);
        writer.flush();
    }

    public void writeln(String line) throws IOException {
        bodyContent.append(line);
        bodyContent.append("<br />");
    }

    public Path getReportFileName() {
        return reportFileName;
    }

    public void appendSourceFile(String baseDirectory, Path file){
        beginCodeSection(file.toString(), "language-java");
        var javaFilePath = Path.of(baseDirectory, file.toString());
        try {
            var fileInput = new FileInputStream(javaFilePath.toFile());
            var fileContent = new String(fileInput.readAllBytes());
            bodyContent.append(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        endCodeSection();
        bodyContent.append("<hr/>");
    }

    public void appendJavaSourceFiles(String baseDirectory, List<Path> javaFilesToList) {

        bodyContent.append("<h3>list of submitted Java files</h3>");
        bodyContent.append("<ul style='list-style-type: none'>");
        for (var relativeJavaFile: javaFilesToList){
            bodyContent.append(String.format("<li>%s</li>", relativeJavaFile));
        }
        bodyContent.append("</ul>");
        bodyContent.append("<hr/>");

        // now dump all Java files into the report
        for (var relativeJavaFile: javaFilesToList){
            appendSourceFile(baseDirectory, relativeJavaFile);
        }
    }
}
