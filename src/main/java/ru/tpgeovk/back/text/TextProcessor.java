package ru.tpgeovk.back.text;

import com.github.askdrcatcher.jrake.*;
import com.github.askdrcatcher.jrake.util.FileUtil;

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
        text = filterText(text);

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
