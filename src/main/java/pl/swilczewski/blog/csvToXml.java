package pl.swilczewski.blog;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class csvToXml {
    public static void main(String[] args) throws ImpossibleModificationException, IOException {
        Directives directives = new Directives();
        directives.add("beans")
                .attr("xmlns", "http://www.springframework.org/schema/beans")
                .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                .attr("xsi:schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd");

        // Authors.csv
        try (CSVReader reader = new CSVReaderBuilder(new FileReader("src/main/resources/csv/Authors.csv")).withSkipLines(1).build()) {
            List<String[]> r = reader.readAll();
            r.forEach(row -> directives
                    .add("bean").attr("class", "pl.swilczewski.blog.domain.Author")
                    .add("property").attr("name", "id").attr("value", row[0]).up()
                    .add("property").attr("name", "first_name").attr("value", row[1]).up()
                    .add("property").attr("name", "last_name").attr("value", row[2]).up()
                    .add("property").attr("name", "username").attr("value", row[3]).up().up());
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Posts.csv
        try (CSVReader reader = new CSVReaderBuilder(new FileReader("src/main/resources/csv/Posts.csv")).withSkipLines(1).build()) {
            List<String[]> r = reader.readAll();
            r.forEach(row -> directives
                    .add("bean").attr("class", "pl.swilczewski.blog.domain.Post")
                    .add("property").attr("name", "id").attr("value", row[0]).up()
                    .add("property").attr("name", "post_content").attr("value", row[1]).up()
                    .add("property").attr("name", "tags").attr("value", row[2]).up().up());
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Posts_Authors.csv
        try (CSVReader reader = new CSVReaderBuilder(new FileReader("src/main/resources/csv/Posts_Authors.csv")).withSkipLines(1).build()) {
            List<String[]> r = reader.readAll();
            r.forEach(row -> directives
                    .add("bean").attr("class", "pl.swilczewski.blog.domain.PostAuthor")
                    .add("property").attr("name", "id_post").attr("value", row[0]).up()
                    .add("property").attr("name", "id_author").attr("value", row[1]).up().up());
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Comments.csv
        try (CSVReader reader = new CSVReaderBuilder(new FileReader("src/main/resources/csv/Comments.csv")).withSkipLines(1).build()) {
            List<String[]> r = reader.readAll();
            r.forEach(row -> directives
                    .add("bean").attr("class", "pl.swilczewski.blog.domain.Comment")
                    .add("property").attr("name", "id").attr("value", row[0]).up()
                    .add("property").attr("name", "username").attr("value", row[1]).up()
                    .add("property").attr("name", "id_post").attr("value", row[2]).up()
                    .add("property").attr("name", "comment_content").attr("value", row[3]).up().up());
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        // Attachments.csv
        try (CSVReader reader = new CSVReaderBuilder(new FileReader("src/main/resources/csv/Attachments.csv")).withSkipLines(1).build()) {
            List<String[]> r = reader.readAll();
            r.forEach(row -> directives
                    .add("bean").attr("class", "pl.swilczewski.blog.domain.Attachment")
                    .add("property").attr("name", "id_post").attr("value", row[0]).up()
                    .add("property").attr("name", "filename").attr("value", row[1]).up().up());
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        String xml = new Xembler(directives).xml();
        FileWriter fw = new FileWriter("src/main/resources/beans.xml");
        fw.write(xml);
        fw.close();
    }
}
