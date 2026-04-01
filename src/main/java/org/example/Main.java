package org.example;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@JsonIgnoreProperties(ignoreUnknown = true)
class Book {
    public VolumeInfo volumeInfo;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {
        public String title;
        public String[] authors;
        public String description;

    }
}

public class Main {
    static void main(String[] args){

        String keyword;
        try (Scanner scanner = new Scanner(System.in)){

            System.out.println("---- 検索したいキーワードを入力 ----");

            keyword = scanner.nextLine();

        }

        String encodedkeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + encodedkeyword;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try{

            System.out.println("検索中...");
            System.out.println("--------------------");

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 200){

                ObjectMapper mapper = new ObjectMapper();

                var root = mapper.readTree(response.body());
                var items = root.get("items");

                if(items != null && items.isArray()) {

                    System.out.println("--- 検索結果 ---");

                    for (var item : items) {

                        Book book = mapper.treeToValue(item, Book.class);

                        System.out.println("タイトル: " + book.volumeInfo.title);
                        if (book.volumeInfo.authors != null) {
                            System.out.println("著者: " + String.join(",", book.volumeInfo.authors));
                        }
                        System.out.println("--------------------");
                    }
                }
                else if(items == null){
                        System.out.println("検索した結果、お探しの作品は見つけられませんでした。");
                }

            }
        }

        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }

    }
}