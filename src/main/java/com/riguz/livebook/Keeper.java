package com.riguz.livebook;

import com.riguz.livebook.client.LiveBookApiClient;
import com.riguz.livebook.client.dto.TocMeta;

import java.io.IOException;

public class Keeper {
    public static void main(String[] args) throws IOException {
        LiveBookApiClient client = new LiveBookApiClient("ponge", 10);
        TocMeta toc = client.getTocAndIndex();
        System.out.println(toc.getChapters().size());

        //String content = HttpClient.get(contentUrl);
    }
}
