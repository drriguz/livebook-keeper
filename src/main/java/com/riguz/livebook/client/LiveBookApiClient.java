package com.riguz.livebook.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.riguz.livebook.HttpClient;
import com.riguz.livebook.client.dto.TocMeta;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class LiveBookApiClient {
    private final String bookName;
    private final int meapVersion;

    public LiveBookApiClient(String bookName, int meapVersion) {
        this.bookName = bookName;
        this.meapVersion = meapVersion;
    }

    public String getChapterContentUrl(String chapterName) throws IOException {
        final String url;
        try {
            url = new URIBuilder("https://livebook.manning.com/api/book/getBookElement")
                    .addParameter("bookShortNameOrSlug", bookName)
                    .addParameter("bookElementShortName", chapterName)
                    .addParameter("freeEbooksAreOpen", "false")
                    .addParameter("platform", "browser-MacIntel")
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String result = HttpClient.get(url);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        return json.getAsJsonObject("bookElement").getAsJsonPrimitive("contentUrl").getAsString();
    }

    public TocMeta getTocAndIndex() throws IOException {
        String url = String.format("https://dpzbhybb2pdcj.cloudfront.net/%s/v-%d/tocAndIndex.json",
                bookName,
                meapVersion);
        String result = HttpClient.get(url);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        JsonElement chapters = json.getAsJsonObject("toc").getAsJsonArray("parts").get(0);
        Gson gson = new Gson();
        return gson.fromJson(chapters, TocMeta.class);
    }

    public String unlock(String chapter, int paragraph) throws IOException {
        final String url = "https://livebook.manning.com/api/userBook/unlockBookElementContent";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("bookShortName", bookName));
        params.add(new BasicNameValuePair("bookElementShortName", chapter));
        params.add(new BasicNameValuePair("paragraphIDs", paragraph + ""));
        params.add(new BasicNameValuePair("meapVersion", meapVersion + ""));
        params.add(new BasicNameValuePair("isSearch", "false"));
        params.add(new BasicNameValuePair("isFreePreview", "true"));
        params.add(new BasicNameValuePair("logTimings", "false"));
        params.add(new BasicNameValuePair("previewDuration", "0.800000000000001"));
        params.add(new BasicNameValuePair("timezone", "Asia/Shanghai"));
        params.add(new BasicNameValuePair("platform", "browser-MacIntel"));
        String result = HttpClient.post(url, params);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        JsonElement unlocked = json.getAsJsonArray("unlockedParagraphs").get(0)
                .getAsJsonObject().getAsJsonPrimitive("content");
        return unlocked.getAsString();
    }
}
