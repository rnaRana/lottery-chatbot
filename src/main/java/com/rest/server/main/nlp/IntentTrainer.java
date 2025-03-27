package com.rest.server.main.nlp;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.*;

public class IntentTrainer {

    private static final String TRAINING_FILE = "src/main/resources/models/intents.train";
    private static final String MODEL_FILE = "src/main/resources/models/intents-model.bin";

    public static void main(String[] args) {
        trainIntentModel();
    }

    public static void trainIntentModel() {
        try {
            // Read training data
            InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File(TRAINING_FILE));
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, "UTF-8");

            // Convert line stream into DocumentSample stream
            ObjectStream<DocumentSample> sampleStream = new ObjectStream<>() {
                @Override
                public DocumentSample read() throws IOException {
                    String line = lineStream.read();
                    if (line == null) return null;

                    // Expect format: "_label_:category sentence"
                    if (!line.startsWith("_label_:")) {
                        throw new IOException("Invalid format: " + line);
                    }

                    String[] parts = line.split(" ", 2);
                    if (parts.length < 2) {
                        throw new IOException("Invalid format: " + line);
                    }

                    String category = parts[0].replace("_label_:", "").trim();
                    String[] tokens = parts[1].split("\\s+"); // Tokenize sentence

                    return new DocumentSample(category, tokens);
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
            DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, new DoccatFactory());

            // Save the model
            try (OutputStream modelOut = new FileOutputStream(MODEL_FILE)) {
                model.serialize(modelOut);
            }

            System.out.println("✅ Intent classification model trained and saved successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error training intent model: " + e.getMessage());
        }
    }
}
