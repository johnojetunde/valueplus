package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalse(String username);

    List<User> findUsersByDeletedFalse();

    @Modifying
    @Query("update User u set u.deleted = true where u.id = ?1")
    void deleteUser(Long userId);

    @Query(value = "SELECT u from User u where " +
            "u IN (select p.user from ProductOrder p where " +
            "p.createdAt>=?1 " +
            "AND p.createdAt<=?2 " +
            "AND p.status='COMPLETED' " +
            "AND p.user.superAgent.referralCode=?3)")
    Page<User> findActiveSuperAgentUsers(LocalDateTime startDate, LocalDateTime endDate, String superAgentCode, Pageable pageable);

    @Query(value = "SELECT u from User u where " +
            "u IN (select p.user from ProductOrder p where " +
            "p.createdAt>=?1 " +
            "AND p.createdAt<=?2 " +
            "AND p.status='COMPLETED' " +
            "AND p.user.superAgent.referralCode=?3)")
    List<User> findActiveSuperAgentListUsers(LocalDateTime startDate, LocalDateTime endDate, String superAgentCode);


    Long countAllByAgentCodeIsNotNull();

    Optional<User> findByAgentCodeAndDeletedFalse(String agentCode);

    Optional<User> findByReferralCode(String referralCode);

    Page<User> findUserBySuperAgent_ReferralCode(String superAgentCode, Pageable pageable);

    Page<User> findUserByRole_Name(String role, Pageable pageable);

    List<User> findUserBySuperAgent(User superAgent);

    List<User> findUsersBySuperAgent_ReferralCode(String referralCode);
}
