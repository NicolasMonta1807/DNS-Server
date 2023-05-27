package com.dns;

import java.util.List;

public class Query {
    private static int datagramID;
    private static List<Integer> queryFlags;
    private static List<Short> queryCounts;
    private static List<String> queryQuestion;

    public static int getDatagramID() {
        return datagramID;
    }

    public static void setDatagramID(int datagramID) {
        Query.datagramID = datagramID;
    }

    public static List<Integer> getQueryFlags() {
        return queryFlags;
    }

    public static void setQueryFlags(List<Integer> queryFlags) {
        Query.queryFlags = queryFlags;
    }

    public static List<Short> getQueryCounts() {
        return queryCounts;
    }

    public static void setQueryCounts(List<Short> queryCounts) {
        Query.queryCounts = queryCounts;
    }

    public static List<String> getQueryQuestion() {
        return queryQuestion;
    }

    public static void setQueryQuestion(List<String> queryQuestion) {
        Query.queryQuestion = queryQuestion;
    }
}
