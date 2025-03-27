package com.rest.server.main.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String question;
    
    @Column(nullable = false, length = 1000)
    private String answer;
}
