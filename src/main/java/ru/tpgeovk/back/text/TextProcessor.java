package ru.tpgeovk.back.text;

import com.github.askdrcatcher.jrake.*;
import com.github.askdrcatcher.jrake.util.FileUtil;
import com.vk.api.sdk.objects.places.responses.SearchResponse;
import org.springframework.util.StringUtils;

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
            return new HashSet<>();
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

    public static Float compareTexts(String text1, String text2) {
        if (StringUtils.isEmpty(text1) || StringUtils.isEmpty(text2)) {
            return 0f;
        }

        text1 = filterText(text1);
        text2 = filterText(text2);

        Set<String> keyWords1 = extractKeyWords(text1);
        Set<String> keyWords2 = extractKeyWords(text2);

        Set<String> bufferSet = keyWords1;
        bufferSet.retainAll(keyWords2);
        int commonWords = bufferSet.size();

        float totalWords = keyWords1.size() + keyWords2.size() - commonWords;

        return commonWords/totalWords;
    }

    public static Float compareTextsSimple(String title, String post) {
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(title)) {
            return 0f;
        }

        int containing = fuzzyContainRating(title, post);

        return (float)containing;
    }

    public static int fuzzyContainRating(String needle, String text) {
        needle = filterText(needle);
        text = filterText(text);

        String[] words = needle.split("\\s+");
        List<String> textWords = Arrays.asList(text.split("\\s+"));
        int rating = 0;

        for (String word : words) {
            if (textWords.contains(word)) {
                rating = rating + 1;
            }
        }

        return rating;
    }
}
