package com.valueplus.persistence.repository;

import com.valueplus.persistence.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}
