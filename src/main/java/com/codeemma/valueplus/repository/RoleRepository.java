package com.codeemma.valueplus.repository;

import com.codeemma.valueplus.model.Role;
import com.codeemma.valueplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
