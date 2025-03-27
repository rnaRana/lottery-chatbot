package com.rest.server.main.repository;

import com.rest.server.main.model.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
	@Query("SELECT qa FROM QuestionAnswer qa WHERE LOWER(qa.question) = LOWER(:question)")
	Optional<QuestionAnswer> findByQuestion(@Param("question") String question);
}
