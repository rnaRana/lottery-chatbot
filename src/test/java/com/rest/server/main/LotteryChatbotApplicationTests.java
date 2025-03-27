package com.rest.server.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

import com.rest.server.main.service.ChatBotService; // <-- Import your service class

@SpringBootTest
class LotteryChatbotApplicationTests {

    
	@MockBean
    private ChatBotService chatBotService;  // <-- Mock the service here

    @Test
    void contextLoads() {
        assertThat(chatBotService).isNotNull();  // Simple assertion to verify context loads
    }
}
