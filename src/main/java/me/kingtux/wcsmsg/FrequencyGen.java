package me.kingtux.wcsmsg;

import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.filter.UrlFilter;
import com.kennycason.kumo.nlp.normalize.BubbleTextNormalizer;
import com.kennycason.kumo.nlp.normalize.CharacterStrippingNormalizer;
import com.kennycason.kumo.nlp.normalize.LowerCaseNormalizer;
import com.kennycason.kumo.nlp.normalize.TrimToEmptyNormalizer;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class FrequencyGen {
    private static final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();

    static {
        //frequencyAnalyzer.setMinWordLength(1);
        frequencyAnalyzer.setWordFrequenciesToReturn(Integer.MAX_VALUE);
        frequencyAnalyzer.addNormalizer(new CharacterStrippingNormalizer());
        frequencyAnalyzer.addNormalizer(new LowerCaseNormalizer());
        frequencyAnalyzer.addNormalizer(new TrimToEmptyNormalizer());
        //frequencyAnalyzer.addFilter(new UrlFilter());
    }

    public static void generate(File exports) {
        String[] directories = exports.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        for (String directory : directories) {
            generateFrequencyReports(new File(exports, directory));

        }

    }

    public static void generateFrequencyReports(File directory) {
        generateFrequencyReport(new File(directory, "texts.split.txt"), new File(directory, "texts.csv"));

    }

    public static void generateFrequencyReport(File file, File export) {

        List<WordFrequency> wordFrequencies = null;
        try {
            wordFrequencies = frequencyAnalyzer.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wordFrequencies == null) return;
        try (CSVWriter writer = new CSVWriter(new FileWriter(export))) {
            for (WordFrequency wordFrequency : wordFrequencies) {
                writer.writeNext(new String[]{wordFrequency.getWord(), String.valueOf(wordFrequency.getFrequency())});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
