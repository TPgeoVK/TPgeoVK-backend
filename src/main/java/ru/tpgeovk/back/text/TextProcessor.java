package ru.tpgeovk.back.text;

import com.github.askdrcatcher.jrake.*;
import com.github.askdrcatcher.jrake.util.FileUtil;
import com.vk.api.sdk.objects.places.responses.SearchResponse;

import java.io.IOException;
import java.util.*;

public class TextProcessor {

    public static String filterText(String text) {
        text = text.trim()
                .replaceAll("[^a-zA-Zа-яА-Я]", " ")
                .toLowerCase()
                .replaceAll(" +", " ");
        return text;
    }

    public static Set<String> extractKeyWords(String text) {

       final Rake rakeInstance = new Rake();

        final Sentences sentences = new SentenceTokenizer().split(text);
        final StopList stopList;
        try {
            stopList = new StopList().generateStopWords(new FileUtil("SmartStoplist.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        final CandidateList candidateList = new CandidateList().generateKeywords(sentences, stopList.getStopWords());

        final Map<String, Double> wordScore = rakeInstance.calculateWordScores(candidateList.getPhraseList());
        final Map<String, Double> keywordCandidates =
                rakeInstance.generateCandidateKeywordScores(candidateList.getPhraseList(), wordScore);

        Map<String, Double> sortedWords = rakeInstance.sortKeyWordCandidates(keywordCandidates);

        /** TODO: фильтрация по весу */
        Set<String> keyWords = new HashSet<>();
        for (String words : sortedWords.keySet()) {
            keyWords.addAll(Arrays.asList(words.split(" ")));
        }

        return keyWords;
    }

    public static Double compareTexts(String text1, String text2) {
        text1 = filterText(text1);
        text2 = filterText(text2);

        Set<String> keyWords1 = extractKeyWords(text1);
        Set<String> keyWords2 = extractKeyWords(text2);

        Set<String> minSet = keyWords1.size() < keyWords2.size() ? keyWords1 : keyWords2;
        Set<String> maxSet = keyWords1 == minSet ? keyWords2 : keyWords1;
        int commonWords = 0;
        for (String word : minSet) {
            if (maxSet.contains(word)) {
                commonWords++;
            }
        }
        double totalWords = keyWords1.size() + keyWords2.size() - commonWords;

        return commonWords/totalWords;
    }

    public int fuzzyContainRating(String needle, String text) {
        needle = filterText(needle);
        text = filterText(text);

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
