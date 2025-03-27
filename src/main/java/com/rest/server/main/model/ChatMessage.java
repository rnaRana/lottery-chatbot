package com.rest.server.main.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="chat_message")
public class ChatMessage {
	@Id
    private String message;
}
