package com.rest.server.main.nlp;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.*;

public class NERTrainer {

    private static final String TRAINING_FILE = "src/main/resources/models/lottery-ner.train";
    private static final String MODEL_FILE = "src/main/resources/models/lottery-ner-model.bin";

    public static void main(String[] args) {
        trainNERModel();
    }

    public static void trainNERModel() {
        try {
            // Read training data
            InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File(TRAINING_FILE));
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, "UTF-8");

            // Convert String lines into NameSample objects
            ObjectStream<NameSample> sampleStream = new ObjectStream<>() {
                @Override
                public NameSample read() throws IOException {
                    String line = lineStream.read();
                    if (line == null) return null;
                    return NameSample.parse(line, true);
                }

                @Override
                public void reset() throws IOException {
                    lineStream.reset();
                }

                @Override
                public void close() throws IOException {
                    lineStream.close();
                }
            };

            // Set training parameters
            TrainingParameters params = new TrainingParameters();
            params.put(TrainingParameters.ITERATIONS_PARAM, "100");
            params.put(TrainingParameters.CUTOFF_PARAM, "1");

            // Train the model
            TokenNameFinderModel model = NameFinderME.train("en", null, sampleStream, params, new TokenNameFinderFactory());

            // Save the model
            try (OutputStream modelOut = new FileOutputStream(MODEL_FILE)) {
                model.serialize(modelOut);
            }

            System.out.println("✅ NER model trained and saved successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error training NER model: " + e.getMessage());
        }
    }
}
