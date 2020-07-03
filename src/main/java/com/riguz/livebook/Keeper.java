package com.riguz.livebook;

import com.riguz.livebook.client.LiveBookApiClient;
import com.riguz.livebook.client.dto.Chapter;
import com.riguz.livebook.client.dto.TocMeta;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Keeper {
    private final LiveBookApiClient client = new LiveBookApiClient("ponge", 10);

    public void saveEncrypted() throws IOException {
        TocMeta toc = client.getTocAndIndex();
        System.out.println(toc.getChapters().size());

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (Chapter chapter : toc.getChapters()) {
            CompletableFuture<Boolean> contentFuture = CompletableFuture.supplyAsync(() -> {
                String contentUrl = null;
                try {
                    contentUrl = client.getChapterContentUrl(chapter.getShortName());
                    System.out.println(chapter.getShortName() + " " + contentUrl);
                    String content = HttpClient.get(contentUrl);
                    Path outPath = Paths.get("chapters/" + chapter.getShortName() + ".html");
                    Files.write(outPath, content.getBytes());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            });
            futures.add(contentFuture);
        }
        CompletableFuture<Boolean>[] arr = futures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(arr).join();
        System.out.println("Saved content to files");
    }

    public void unlock(int chapter) throws IOException {
        String contentPath = String.format("chapters/chapter-%d.html", chapter);
        String chapterContent = new String(Files.readAllBytes(Paths.get(contentPath)));
        Document doc = Jsoup.parse(chapterContent);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (Element element : doc.select("div.scrambled")) {
            int paragraphId = Integer.parseInt(element.attr("id"));
            CompletableFuture<Boolean> contentFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Path outPath = Paths.get("chapters/" + "chapter-" + chapter + "-" + paragraphId + ".html");
                    if (Files.exists(outPath))
                        return true;
                    String unlockedParagraph = client.unlock("chapter-" + chapter, paragraphId);
                    element.text(unlockedParagraph);

                    Files.write(outPath, unlockedParagraph.getBytes());
                    System.out.println("Unlocked: chapter-" + chapter + " p:" + element.attr("id"));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
            futures.add(contentFuture);
        }
        CompletableFuture<Boolean>[] arr = futures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(arr).join();
        System.out.println("Decrypted chapter.");
        Path outPath = Paths.get("chapters/" + "chapter-" + chapter + "-unlocked.html");
        Files.write(outPath, doc.outerHtml().getBytes());
    }

    public static void main(String[] args) throws IOException {
        Keeper keeper = new Keeper();
        //for (int i = 2; i <= 13; i++)
            keeper.unlock(13);
    }
}
