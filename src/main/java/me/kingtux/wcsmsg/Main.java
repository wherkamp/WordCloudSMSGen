package me.kingtux.wcsmsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("SMS_PARSER");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Please Provide a file");
            return;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("Please Provide a file");
            return;
        }

        File export = new File("export");
        SourceParser.parse(file, export);
        FrequencyGen.generate(export);
        WordCloudGen.generateWordClouds(export);
    }
}
