package com.rest.server.main.controller;

import com.rest.server.main.model.ChatMessage;
import com.rest.server.main.model.QuestionAnswer;
import com.rest.server.main.service.ChatBotService;
import com.rest.server.main.repository.QuestionAnswerRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller  // Handles UI rendering
@RequestMapping("/chatbot")
public class ChatBotController {
	
	private final static Logger logger = LogManager.getLogger(ChatBotController.class);

    private final ChatBotService chatBotService;

    @Autowired
    public ChatBotController(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    // Load Chatbot UI (Thymeleaf)
    @GetMapping
    public String chatbotPage() {
        return "chatbot"; // Loads chatbot.html from templates folder
    }
}

// Separate API Controller for chatbot responses
@RestController // API for frontend interactions
@RequestMapping("/api/chatbot")
class ChatBotRestController {
	
	private final static Logger logger = LogManager.getLogger(ChatBotRestController.class);

    private final ChatBotService chatBotService;
    private final QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    public ChatBotRestController(ChatBotService chatBotService, QuestionAnswerRepository questionAnswerRepository) {
        this.chatBotService = chatBotService;
        this.questionAnswerRepository = questionAnswerRepository;
    }

    // Handle user messages via API
    @PostMapping("/message")
    public ResponseEntity<String> chat(@RequestBody ChatMessage chatMessage) {
        try {
            logger.info("🔹 Received request: " + chatMessage.getMessage()); // Debug Log

            if (chatMessage.getMessage() == null || chatMessage.getMessage().isBlank()) {
                logger.info("⚠️ Error: Empty message received");
                return ResponseEntity.badRequest().body("Message cannot be empty.");
            }

            String response = chatBotService.getResponse(chatMessage.getMessage());
            logger.info("🤖 Bot Response: " + response); // Debug Log

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.info("❌ Error processing chatbot request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An error occurred: " + e.getMessage());
        }
    }

    // Add New Q&A to Database
    @PostMapping("/addQA")
    public ResponseEntity<String> addQuestionAnswer(@RequestBody QuestionAnswer qa) {
        try {
            logger.info("📥 Adding Q&A: " + qa.getQuestion() + " → " + qa.getAnswer()); // Debug Log

            if (qa.getQuestion() == null || qa.getAnswer() == null || qa.getQuestion().isBlank() || qa.getAnswer().isBlank()) {
                logger.info("⚠️ Error: Question or answer is empty");
                return ResponseEntity.badRequest().body("Question and answer cannot be empty.");
            }

            questionAnswerRepository.save(qa);
            logger.info("✅ Q&A successfully saved in database.");
            return ResponseEntity.ok("Q&A added successfully.");
        } catch (Exception e) {
            logger.info("❌ Error saving Q&A: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An error occurred while saving Q&A: " + e.getMessage());
        }
    }
}
