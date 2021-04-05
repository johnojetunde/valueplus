package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.BadRequestException;
import com.valueplus.domain.model.AuthorityModel;
import com.valueplus.persistence.entity.Authority;
import com.valueplus.persistence.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.valueplus.domain.util.FunctionUtil.emptyIfNullStream;

@Service
@RequiredArgsConstructor
public class UserUtilService {
    private final AuthorityRepository authorityRepository;

    public List<Authority> getAdminAuthority(Set<Long> authorities) {
        List<Authority> userAuthority;
        if (authorities == null || authorities.isEmpty()) {
            throw new BadRequestException("No authority selected for ADMIN user");
        }

        userAuthority = authorityRepository.findAllById(authorities);
        if (userAuthority.size() != authorities.size()) {
            throw new BadRequestException("Could not find matching authorities");
        }
        return userAuthority;
    }

    public List<AuthorityModel> getAllAuthorities() {
        return emptyIfNullStream(authorityRepository.findAll())
                .map(Authority::toModel)
                .collect(Collectors.toList());
    }
}
