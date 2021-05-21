package com.valueplus.persistence.entity;

import com.valueplus.domain.model.AuthorityModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "authority")
public class Authority extends BasePersistentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String authority;

    public AuthorityModel toModel() {
        return new AuthorityModel(this.getId(), this.getAuthority());
    }
}
