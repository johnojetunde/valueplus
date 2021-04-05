package com.valueplus.app.controller;

import com.valueplus.domain.enums.ActionType;
import com.valueplus.domain.enums.EntityType;
import com.valueplus.domain.model.AuditLogModel;
import com.valueplus.domain.model.AuditModel;
import com.valueplus.domain.service.abstracts.AuditService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.valueplus.domain.enums.ActionType.*;
import static com.valueplus.domain.enums.EntityType.*;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "v1/audits", produces = APPLICATION_JSON_VALUE)
public class AuditController {
    private final AuditService auditService;

    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOG')")
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Page<AuditLogModel> query(@Valid @RequestBody AuditModel queryModel,
                                     @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws Exception {
        return auditService.query(queryModel, pageable);
    }

    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOG')")
    @GetMapping("/entity-action-mapping")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = Map.class)})
    public Map<EntityType, List<ActionType>> getEntityActionMapping() {
        return getEntityActionMap();
    }

    private Map<EntityType, List<ActionType>> getEntityActionMap() {
        Map<EntityType, List<ActionType>> entityTypeActions = new HashMap<>();
        entityTypeActions.put(USER, List.of(
                USER_LOGIN,
                USER_PROFILE_UPDATE,
                USER_PIN_UPDATE,
                USER_CREATE_AGENT,
                USER_CREATE_SUPER_AGENT,
                USER_CREATE_ADMIN,
                USER_AUTHORITY_UPDATE,
                USER_PASSWORD_UPDATE,
                USER_PASSWORD_RESET,
                USER_PROFILE_PICTURE_UPDATE
        ));
        entityTypeActions.put(ACCOUNT, List.of(ACCOUNT_CREATE));
        entityTypeActions.put(PRODUCT_ORDER, List.of(
                PRODUCT_ORDER_CREATE,
                PRODUCT_ORDER_STATUS_UPDATE
        ));
        entityTypeActions.put(PRODUCT, List.of(
                PRODUCT_CREATE,
                PRODUCT_UPDATE
        ));
        entityTypeActions.put(TRANSACTION, List.of(
                TRANSACTION_INITIATE,
                TRANSACTION_STATUS_CHANGE
        ));
        entityTypeActions.put(SETTING, List.of(SETTING_CHANGE));

        return entityTypeActions;
    }
}
