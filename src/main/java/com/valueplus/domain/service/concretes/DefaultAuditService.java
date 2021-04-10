package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.exception.ValuePlusRuntimeException;
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
import java.util.Optional;

import static com.valueplus.domain.util.MapperUtil.MAPPER;
import static com.valueplus.domain.util.UserUtils.getLoggedInUser;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Slf4j
@Service
public class DefaultAuditService implements AuditService {
    private final AuditLogRepository repository;
    private final AuditEntityConverterService converterService;

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
                    .actor(getAuthenticatedUser().orElse(null))
                    .build();

            log.debug("Saving audit log with data {}", auditLog);
            repository.save(auditLog);
        } catch (Exception e) {
            throw new ValuePlusException("Error saving audit log", e);
        }
    }

    private Optional<User> getAuthenticatedUser() {
        try {
            return ofNullable(getLoggedInUser());
        } catch (Exception e) {
            return Optional.empty();
        }
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
                .actor(ofNullable(auditLog.getActor()).map(this::getActorDetails).orElse(null))
                .build();
    }

    private ActorDetails getActorDetails(User actor) {
        return new ActorDetails(actor.getId(), actor.getEmail(), actor.getFirstname(), actor.getLastname());
    }
}
