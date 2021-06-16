package com.riguz.livebook;

import com.riguz.livebook.client.LiveBookApiClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Keeper {
    private final LiveBookApiClient client = new LiveBookApiClient("ponge", 10);

    public void generate(int chapter) throws IOException {
        String contentPath = String.format("chapters/chapter-%d.html", chapter);
        String chapterContent = new String(Files.readAllBytes(Paths.get(contentPath)));
        Document doc = Jsoup.parse(chapterContent);

        for (Element element : doc.select("div.scrambled")) {
            int paragraphId = Integer.parseInt(element.attr("id"));
            Path unlockedPath = Paths.get("chapters/" + "chapter-" + chapter + "-" + paragraphId + ".html");
            String unlockedParagraph = new String(Files.readAllBytes(unlockedPath));
            if (element.select("p").size() != 1)
                throw new RuntimeException("Why?");
            Document pDoc = Jsoup.parse(unlockedParagraph);
            element.selectFirst("p").text(pDoc.selectFirst("p").text());
        }


        for (Element element : doc.select("img")) {
            String url = element.attr("src");
            String localPath = url.substring("{{BOOK_ROOT_FOLDER}}".length() + 1);
            String realImageUrl = "https://drek4537l1klr.cloudfront.net/" + localPath;
            File localFile = new File(localPath);
            localFile.getParentFile().mkdirs();
            element.attr("src", localPath);
            if (!localFile.exists())
                HttpClient.download(realImageUrl, localFile);
        }

        System.out.println("Decrypted chapter." + chapter);
        applyStyles(doc);

        Path outPath = Paths.get("unlocked/" + "chapter-" + chapter + ".html");

        Files.write(outPath, doc.outerHtml().getBytes());
    }


    private void applyStyles(Document doc) {
        doc.head().append("<link rel=\"stylesheet\" href=\"style.css\">");
        doc.head().append("<link rel=\"stylesheet\"\n" +
                "      href=\"//cdnjs.cloudflare.com/ajax/libs/highlight.js/10.1.2/styles/default.min.css\">\n" +
                "<script src=\"//cdnjs.cloudflare.com/ajax/libs/highlight.js/10.1.2/highlight.min.js\"></script>");
        doc.head().append("<script src=\"highlight.js\"></script>");
    }

    public static void main(String[] args) throws IOException {
        Keeper keeper = new Keeper();
        for (int i = 1; i <= 13; i++)
            keeper.generate(i);
    }
}
