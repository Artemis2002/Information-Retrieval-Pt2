package com.company;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        String txtFilesDir = "src\\com\\company";
        Directory dir = FSDirectory.open(Paths.get("index"));
        try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {


            try (Stream<Path> paths = Files.walk(Paths.get(txtFilesDir))) {
                paths.filter(Files::isRegularFile)
                        .forEach(file -> {
                            try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
                                StringBuilder content = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    content.append(line).append("\n");
                                }

                                Document doc = new Document();
                                doc.add(new StringField("filename", file.getFileName().toString(), Field.Store.YES));
                                doc.add(new TextField("content", content.toString(), Field.Store.YES));
                                writer.addDocument(doc);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your query: ");
        String q1 = scanner.nextLine();

        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        Query query = parser.parse(q1);
        try (IndexReader reader = DirectoryReader.open(dir)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs results = searcher.search(query, 1);

            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                System.out.println(doc.get("filename"));
                System.out.println(doc.get("content"));
            }
        }

    }
}
