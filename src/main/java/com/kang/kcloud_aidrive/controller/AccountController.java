package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.controller.req.AccountLoginReq;
import com.kang.kcloud_aidrive.controller.req.AccountRegisterReq;
import com.kang.kcloud_aidrive.dto.AccountDTO;
import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import com.kang.kcloud_aidrive.service.AccountService;
import com.kang.kcloud_aidrive.util.JsonData;
import com.kang.kcloud_aidrive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping("register")
    @Tag(name = "Account APIs", description = "Account-Register API")
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
                                                "msg": "error message",
                                                "success": false
                                            }
                                            """
                            )))
            }
    )
    public ResponseEntity<JsonData> register(@RequestBody
                                             @Parameter(
                                                     description = "Account registration request",
                                                     required = true,
                                                     content = @Content(schema = @Schema(implementation = AccountRegisterReq.class))
                                             ) AccountRegisterReq req) {
        try {
            accountService.register(req);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(JsonData.buildError(e.getMessage()));
        }
        return ResponseEntity.ok(JsonData.buildSuccess("Account registered successfully"));
    }

    /**
     * Account Avatar Upload Interface
     */

    @PostMapping("avatar")
    @Tag(name = "Account APIs", description = "Avatar Upload API")
    @Operation(summary = "Register a new account", description = "Registers a new account",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code": 0,
                                                "data": {},
                                                "msg": "Avatar uploaded successfully - URL",
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
                                                "msg": "error message",
                                                "success": false
                                            }
                                            """
                            )))
            }
    )
    public ResponseEntity<JsonData> uploadAvatar(@RequestParam("file")
                                                 @Parameter(
                                                         description = "Avatar upload request",
                                                         required = true,
                                                         content = @Content(mediaType = "multipart/form-data")
                                                 ) MultipartFile file) {
        try {
            String url = accountService.uploadAvatar(file);
            return ResponseEntity.ok(JsonData.buildSuccess("Avatar uploaded successfully - " + url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(JsonData.buildError(e.getMessage()));
        }
    }

    @PostMapping("login")
    @Tag(name = "Account APIs", description = "Account User Login API")
    @Operation(summary = "Account User Login", description = "Account User Login",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code": 0,
                                                "data": {},
                                                "msg": "token",
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
                                                "msg": "error message",
                                                "success": false
                                            }
                                            """
                            )))
            }
    )
    public ResponseEntity<JsonData> login(@RequestBody
                                          @Parameter(
                                                  description = "Account registration request",
                                                  required = true,
                                                  content = @Content(schema = @Schema(implementation = AccountLoginReq.class))
                                          )
                                          AccountLoginReq req) {
        AccountDTO accountDTO = accountService.login(req);

        // jwt token - front end (localStorage or sessionStorage)
        String token = JwtUtil.geneLoginJWT(accountDTO);

        return ResponseEntity.ok(JsonData.buildSuccess(token));
    }

    @GetMapping("detail")
    @Tag(name = "Account APIs", description = "User Account Detail API")
    @Operation(summary = "User Account Detail", description = "User Account Detail",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JsonData.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "code": 0,
                                                "data": {},
                                                "msg": "Account Detail",
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
                                                "msg": "error message",
                                                "success": false
                                            }
                                            """
                            )))
            }
    )
    public ResponseEntity<JsonData> detail() {
        AccountDTO accountDTO = accountService.queryDetail(LoginInterceptor.threadLocal.get().getId());
        return ResponseEntity.ok(JsonData.buildSuccess(accountDTO));
    }
}
