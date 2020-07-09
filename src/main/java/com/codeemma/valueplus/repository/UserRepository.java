package com.codeemma.valueplus.repository;

import com.codeemma.valueplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalse(String username);

    @Modifying
    @Query("update User u set u.deleted = true where u.id = ?1")
    void deleteUser(Long userId);
}
