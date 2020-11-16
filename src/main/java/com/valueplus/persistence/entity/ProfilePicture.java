package com.valueplus.persistence.entity;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "profile_picture")
public class ProfilePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @Lob
    @Type(type="org.hibernate.type.BinaryType")
    @Column(columnDefinition = "BYTEA")
    private byte[] photo;
}
