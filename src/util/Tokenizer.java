package util;

public class Tokenizer {
    public static String[] tokenize(String sentence) {
        return sentence.split("[^\\w]+");
    }

}
