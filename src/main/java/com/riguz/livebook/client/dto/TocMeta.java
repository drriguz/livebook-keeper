package com.riguz.livebook.client.dto;

import java.util.List;

public class TocMeta {
    private List<Chapter> chapters;

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}
