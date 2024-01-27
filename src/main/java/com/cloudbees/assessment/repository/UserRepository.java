package com.cloudbees.assessment.repository;

import com.cloudbees.assessment.entity.User;
import com.cloudbees.assessment.enums.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    User findByEmailOrId(String email, Long userId);

    List<User> findBySection(Section section);

    Optional<User> findById(Long userId);
}
