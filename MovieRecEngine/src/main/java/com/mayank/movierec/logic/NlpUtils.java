package com.mayank.movierec.logic;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NlpUtils {

    private final SimpleTokenizer tokenizer;
    private final PorterStemmer stemmer;

    public NlpUtils() {
        tokenizer = SimpleTokenizer.INSTANCE;
        stemmer = new PorterStemmer();
    }

    /**
     * Tokenizes and stems the input text.
     * @param text The text to process.
     * @return A space-separated string of stemmed words in lowercase.
     */
    public String getStemmedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        // Tokenize (split into words) and convert to lowercase
        String[] tokens = tokenizer.tokenize(text.toLowerCase());

        // Stem each token and join back into a string
        return Arrays.stream(tokens)
                .map(stemmer::stem)
                .collect(Collectors.joining(" "));
    }

    /**
     * Stems a single word.
     * @param word The word to stem.
     * @return The stemmed word in lowercase.
     */
    public String stemWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return "";
        }
        return stemmer.stem(word.toLowerCase());
    }
}