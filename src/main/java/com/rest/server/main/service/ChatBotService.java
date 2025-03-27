package com.rest.server.main.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rest.server.main.model.QuestionAnswer;
import com.rest.server.main.repository.QuestionAnswerRepository;

import jakarta.annotation.PostConstruct;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

@Service
public class ChatBotService {
	
	private final static Logger logger = LogManager.getLogger(ChatBotService.class);

    private final QuestionAnswerRepository questionAnswerRepository;
    private DoccatModel intentModel;
    private TokenNameFinderModel nerModel;

    @Autowired
    public ChatBotService(QuestionAnswerRepository questionAnswerRepository) {
        this.questionAnswerRepository = questionAnswerRepository;
    }

    @PostConstruct
    public void init() {
        try {
            // Load trained intent model
            try (InputStream intentStream = getClass().getClassLoader().getResourceAsStream("models/intents-model.bin")) {
                if (intentStream == null) throw new RuntimeException("Intent model file not found.");
                intentModel = new DoccatModel(intentStream);
            }

            // Load trained NER model
            try (InputStream nerStream = getClass().getClassLoader().getResourceAsStream("models/lottery-ner-model.bin")) {
                if (nerStream == null) throw new RuntimeException("NER model file not found.");
                nerModel = new TokenNameFinderModel(nerStream);
            }

            logger.info("✅ Models loaded successfully!");

        } catch (Exception e) {
            throw new RuntimeException("Error loading models: " + e.getMessage());
        }
    }

    public String getResponse(String input) {
        if (intentModel == null || nerModel == null) {
            throw new IllegalStateException("Models are not loaded.");
        }

        logger.info("📝 Received message: " + input);

        // Detect intent
        String intent = detectIntent(input);
        logger.info("🎯 Detected intent: " + intent);

        // Extract entities
        List<String> entities = extractEntities(input);
        logger.info("🔍 Extracted entities: " + entities);

        // Check database for stored answers
        Optional<QuestionAnswer> dbResponse = questionAnswerRepository.findByQuestion(input);
        if (dbResponse.isPresent()) {
            logger.info("📌 Found answer in database: " + dbResponse.get().getAnswer());
            return dbResponse.get().getAnswer();
        }

        // Return response based on detected intent and entities
        return generateResponse(intent, entities);
    }

    private String detectIntent(String sentence) {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(intentModel);
        double[] outcomes = categorizer.categorize(sentence.split(" "));
        return categorizer.getBestCategory(outcomes);
    }

    private List<String> extractEntities(String sentence) {
        NameFinderME nameFinder = new NameFinderME(nerModel);
        String[] tokens = sentence.split(" "); // Use space-based tokenization instead

        //Detect named entities
        var spans = nameFinder.find(tokens);
        

        // Debugging logs
        logger.info("🔍 Tokens: " + Arrays.toString(tokens));
        for(var span : spans) {
        	logger.info("🔍 Detected entity: " + span.toString());
        }
        
        //Extract and return entity names
        List<String> entities = new ArrayList<>();
        
        // i removed nameFinder.find(tokens) and replace with spans in generic for loop
        for (var span : spans) {
            // Join words that form an entity
            String entity = String.join(" ", Arrays.copyOfRange(tokens, span.getStart(), span.getEnd()));
            entities.add(entity);
        }
        return entities;
    }

    private String generateResponse(String intent, List<String> entities) {
        if (intent.equals("greeting")) return "Hello! How can I assist you today?";
        if (intent.equals("farewell")) return "Goodbye! Have a great day!";
        if (intent.equals("thanks")) return "You're welcome!";
        if (intent.equals("question")) {
        	if (!entities.isEmpty()) {
        	    return "I see you're asking about " + String.join(", ", entities) + ". Can you clarify?";
        	} else {
                return "That's an interesting question! Can you clarify?";
            }
        }
        return "I'm not sure how to respond to that.";
    }
}
