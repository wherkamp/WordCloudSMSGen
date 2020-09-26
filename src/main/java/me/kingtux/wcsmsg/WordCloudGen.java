package me.kingtux.wcsmsg;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class WordCloudGen {
    public static void generateWordClouds(File exports) {
        String[] directories = exports.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        for (String directory : directories) {
            System.out.println("directory = " + directory);
            generateWordCloud(new File(exports, directory));

        }

    }

    public static void generateWordCloud(File export) {
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        //frequencyAnalyzer.setFilter(new SimpleFilter(discordCloud.getGuildBannedWords(message.getGuild())));
        List<WordFrequency> wordFrequencies = null;
        try {
            wordFrequencies = frequencyAnalyzer.load(new File(export, "texts.split.txt"));
        } catch (IOException e) {
            return;
        }
        try {
            Dimension dimension = new Dimension(1024, 768);
            WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
            wordCloud.setBackground(new CircleBackground(300));
            wordCloud.setBackgroundColor(Color.LIGHT_GRAY);
            wordCloud.setColorPalette(new ColorPalette(Color.DARK_GRAY));
            wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
            wordCloud.setPadding(2);
            wordCloud.build(wordFrequencies);
            wordCloud.writeToFile(new File(export, System.currentTimeMillis() + ".png").getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Color[] getColors() {
        Random r = new Random();
        Color[] colors = new Color[100];
        for (int i = 0; i < 100; i++) {
            colors[i] = new Color(156 + r.nextInt(100), 156 + r.nextInt(100), 156 + r.nextInt(100));
        }
        return colors;
    }
}
