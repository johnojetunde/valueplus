package com.valueplus.persistence.entity.audit_mappers;

import com.valueplus.domain.enums.EntityType;
import com.valueplus.persistence.entity.ProfilePicture;
import org.springframework.stereotype.Component;

@Component
public class ProfilePictureEntityConverter implements AuditEntityToModel<ProfilePicture> {
    @Override
    public Class<ProfilePicture> clazz() {
        return ProfilePicture.class;
    }

    @Override
    public EntityType entityType() {
        return EntityType.USER_PROFILE_PICTURE;
    }
}

