package ru.tpgeovk.back.text;

public class TextProcessor {

    public static String filterText(String text) {
        text = text.trim()
                .replaceAll("[^a-zA-Zа-яА-Я]", " ")
                .toLowerCase()
                .replaceAll(" +", " ");
        return text;
    }

    public int fuzzyContainRating(String needle, String text) {
        needle = needle.toLowerCase().replace('#', ' ').replace('_', ' ');
        text = text.toLowerCase().replace('#', ' ').replace('_', ' ');

        String[] words = needle.split("\\s+");
        int rating = 0;

        for (String word : words) {
            if (text.contains(word)) {
                rating = rating + 1;
            }
        }

        return rating;
    }
}
