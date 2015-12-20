package util;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by Xianyan Jia on 26/10/2015.
 */
public class TextSimilarity {
    public static double getSimilarity2(String[] base, String compareSentence) {
        List<String> baseSentences = new ArrayList<String>(Arrays.asList(base));
        List<String> compareSentences = new ArrayList<String>(Arrays.asList(compareSentence.split("\t")));
        return getSimilarity(baseSentences, compareSentences);
    }

    public static double getSimilarity(String[] base, String compareSentence) {
        double score = 0.0;
        compareSentence = compareSentence.toLowerCase();
        for (String keyword : base) {
            keyword = keyword.toLowerCase();
            if (compareSentence.indexOf(keyword) > 0) {
                score += StringUtils.countMatches(compareSentence, keyword);
            }
        }
        return score;
    }


    public static double getSimilarity(List<String> baseSentence, List<String> compareSentence) {
        Set<String> sentencesWords = new TreeSet<String>();
        sentencesWords.addAll(baseSentence);
        sentencesWords.addAll(compareSentence);

        List<Integer> baseSentenceFrequency = new ArrayList<Integer>();
        List<Integer> compareSentenceFrequency = new ArrayList<Integer>();

        for (String word : sentencesWords) {
            baseSentenceFrequency.add(getWordFrequency(word, baseSentence));
            compareSentenceFrequency.add(getWordFrequency(word, compareSentence));
        }

        int vectorProduct = 0, baseNorm = 0, compareNorm = 0;
        for (int i = 0; i < sentencesWords.size(); i++) {
            vectorProduct += baseSentenceFrequency.get(i) * compareSentenceFrequency.get(i);
            baseNorm += Math.pow(baseSentenceFrequency.get(i), 2);
            compareNorm += Math.pow(compareSentenceFrequency.get(i), 2);
        }

        return vectorProduct / (Math.sqrt(baseNorm) * Math.sqrt(compareNorm));
    }

    private static int getWordFrequency(String word, List<String> sentence) {
        int frequencyValue = 0;
        for (String sentenceWord : sentence) {
            if (sentenceWord.equals(word)) {
                ++frequencyValue;
            }
        }
        return frequencyValue;
    }
}
