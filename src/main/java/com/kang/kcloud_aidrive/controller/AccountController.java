package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.service.AccountService;
import com.kang.kcloud_aidrive.util.JsonData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account Controller API
 * This API provides endpoints for managing accounts.
 *
 * @author Kai Kang
 * @since 2025-01-19
 */

@RestController
@RequestMapping("/api/account/v1")
@Tag(name = "Account APIs", description = "Account APIs")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Account Registration Interface
     * Registers a new account.
     *
     * @param req Account registration request
     * @return Success response
     */
    @PostMapping("/register")
    @Tag(name = "Account APIs", description = "Account-specific API")
    @Operation(summary = "Register a new account", description = "Registers a new account",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code": 0,
                                                "data": {},
                                                "msg": "Account registered successfully",
                                                "success": true
                                            }
                                            """
                            ))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code": -1,
                                                "data": {},
                                                "msg": "",
                                                "success": false
                                            }
                                            """
                            )))
            }
    )
    public ResponseEntity<JsonData> register(@RequestBody AccountRegisterReq req) {
        try {
            accountService.register(req);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(JsonData.buildError(e.getMessage()));
        }
        return ResponseEntity.ok(JsonData.buildSuccess("Account registered successfully"));
    }
}
