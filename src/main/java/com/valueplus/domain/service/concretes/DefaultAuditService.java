package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.NotFoundException;
import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.exception.ValuePlusRuntimeException;
import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.model.AuditLogModel;
import com.valueplus.domain.model.AuditLogModel.ActorDetails;
import com.valueplus.domain.model.AuditModel;
import com.valueplus.domain.service.abstracts.AuditService;
import com.valueplus.persistence.entity.AuditLog;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.entity.audit_mappers.AuditEntityConverterService;
import com.valueplus.persistence.repository.AuditLogRepository;
import com.valueplus.persistence.specs.AuditLogSpecification;
import com.valueplus.persistence.specs.SearchCriteria;
import com.valueplus.persistence.specs.SearchOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.valueplus.domain.enums.ActionType.*;
import static com.valueplus.domain.util.MapperUtil.MAPPER;
import static com.valueplus.domain.util.UserUtils.getLoggedInUser;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Slf4j
@Service
public class DefaultAuditService implements AuditService {
    private final AuditLogRepository repository;
    private final AuditEntityConverterService converterService;
    private final ProfilePictureService profilePictureService;

    private static final Map<ActionType, String> DESCRIPTION_MAP = new HashMap<>();

    static {
        DESCRIPTION_MAP.put(USER_LOGIN, "User logged in");
        DESCRIPTION_MAP.put(USER_PROFILE_UPDATE, "User performed profile update");
        DESCRIPTION_MAP.put(USER_PIN_UPDATE, "User updated pin");
        DESCRIPTION_MAP.put(USER_DISABLE, "User profile disabled");
        DESCRIPTION_MAP.put(USER_ENABLE, "User profile enabled");
        DESCRIPTION_MAP.put(USER_CREATE_AGENT, "Admin created agent");
        DESCRIPTION_MAP.put(USER_CREATE_SUPER_AGENT, "Admin created super agent");
        DESCRIPTION_MAP.put(USER_CREATE_ADMIN, "Admin created an admin profile");
        DESCRIPTION_MAP.put(USER_AUTHORITY_UPDATE, "Admin updated the authority update");
        DESCRIPTION_MAP.put(USER_PASSWORD_UPDATE, "User updated password");
        DESCRIPTION_MAP.put(USER_PASSWORD_RESET, "User performed a password reset");
        DESCRIPTION_MAP.put(USER_PROFILE_PICTURE_UPDATE, "User updated profile picture");
        DESCRIPTION_MAP.put(ACCOUNT_CREATE, "Account created");
        DESCRIPTION_MAP.put(PRODUCT_ORDER_CREATE, "User created a product order");
        DESCRIPTION_MAP.put(PRODUCT_ORDER_STATUS_UPDATE, "ProductOrder status update");
        DESCRIPTION_MAP.put(PRODUCT_CREATE, "Admin created product");
        DESCRIPTION_MAP.put(PRODUCT_UPDATE, "Admin updated product ");
        DESCRIPTION_MAP.put(PRODUCT_STATUS_UPDATE, "Product status was updated");
        DESCRIPTION_MAP.put(PRODUCT_STATUS_ENABLE, "Product was enabled");
        DESCRIPTION_MAP.put(PRODUCT_STATUS_DISABLE, "Product was disabled");
        DESCRIPTION_MAP.put(PRODUCT_DELETE, "Product has been deleted");
        DESCRIPTION_MAP.put(SETTING_CHANGE, "Admin performed a system settings update");
        DESCRIPTION_MAP.put(TRANSACTION_INITIATE, "User initiated a transaction");
        DESCRIPTION_MAP.put(TRANSACTION_STATUS_CHANGE, "User's transaction status updated");
    }

    @Override
    public void save(AuditLogModel model) throws ValuePlusException {
        saveLog(model);
    }

    private void saveLog(AuditLogModel model) throws ValuePlusException {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .newData(MAPPER.writeValueAsString(model.getNewData()))
                    .prevData(MAPPER.writeValueAsString(model.getPreviousData()))
                    .entityType(model.getEntityType())
                    .actionType(model.getAction())
                    .actor(getAuthenticatedUser(model.getNewData(), model.getAction()))
                    .build();

            log.debug("Saving audit log with data {}", auditLog);
            repository.save(auditLog);
        } catch (Exception e) {
            log.error("error saving in audit log ", e);
            throw new ValuePlusException("Error saving audit log", e);
        }
    }

    private User getAuthenticatedUser(Object object, ActionType type) {
        if (USER_LOGIN.equals(type) || USER_PASSWORD_RESET.equals(type)) {
            return MAPPER.convertValue(object, User.class);
        }

        return getLoggedUser().orElse(null);
    }

    @Override
    public Page<AuditLogModel> query(AuditModel model, Pageable pageable) {
        ensureStartDateIsBeforeEndDate(model);
        var specification = buildSpecification(model);
        return repository.findAll(specification, pageable)
                .map(this::toModel);
    }

    private void ensureStartDateIsBeforeEndDate(AuditModel model) {
        ofNullable(model.getStartDate()).ifPresent(startDate ->
                ofNullable(model.getEndDate()).ifPresent(endDate -> {
                    if (startDate.isAfter(endDate))
                        throw new ValuePlusRuntimeException("EndDate cannot be earlier than StartDate");
                }));
    }

    private AuditLogSpecification buildSpecification(AuditModel filter) {
        AuditLogSpecification specification = new AuditLogSpecification();
        if (filter.getEntityType() != null) {
            specification.add(new SearchCriteria<>("entityType", filter.getEntityType(), SearchOperation.EQUAL));
        }
        if (filter.getAction() != null) {
            specification.add(new SearchCriteria<>("actionType", filter.getAction(), SearchOperation.EQUAL));
        }
        if (filter.getStartDate() != null) {
            specification.add(new SearchCriteria<>("createdAt", filter.getStartDate().atStartOfDay(), SearchOperation.GREATER_THAN_EQUAL));
        }
        if (filter.getEndDate() != null) {
            specification.add(new SearchCriteria<>("createdAt", filter.getEndDate().atTime(LocalTime.MAX), SearchOperation.LESS_THAN_EQUAL));
        }

        return specification;
    }

    public AuditLogModel toModel(AuditLog auditLog) {
        return AuditLogModel.builder()
                .previousData(converterService.toObject(auditLog.getPrevData(), auditLog.getEntityType()))
                .newData(converterService.toObject(auditLog.getNewData(), auditLog.getEntityType()))
                .entityType(auditLog.getEntityType())
                .action(auditLog.getActionType())
                .createdAt(auditLog.getCreatedAt())
                .description(DESCRIPTION_MAP.getOrDefault(auditLog.getActionType(), auditLog.toString()))
                .actor(ofNullable(auditLog.getActor()).map(this::getActorDetails).orElse(systemActor()))
                .build();
    }

    private ActorDetails getActorDetails(User actor) {
        String photo = profilePictureService.getImage(actor).orElse(null);
        return new ActorDetails(actor.getId(), actor.getEmail(), actor.getFirstname(), actor.getLastname(), photo);
    }

    private ActorDetails systemActor() {
        String system = "system";
        return new ActorDetails(0L, system, system, system, system);
    }

    private Optional<User> getLoggedUser() {
        try {
            return Optional.of(getLoggedInUser());
        } catch (NotFoundException e) {
            log.debug("No user found");
        }
        return empty();
    }
}
