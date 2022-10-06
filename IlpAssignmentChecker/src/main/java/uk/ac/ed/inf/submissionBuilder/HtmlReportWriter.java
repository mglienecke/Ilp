package uk.ac.ed.inf.submissionBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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

    public void beginSection(String sectionHeader, String sectionClassName) throws IOException {
        bodyContent.append(String.format("<h3>%s</h3><div class='%s'>", sectionHeader, (sectionClassName != null ? sectionClassName : "")));
    }

    public void endSection() throws IOException{
        bodyContent.append("</div>");
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
}
