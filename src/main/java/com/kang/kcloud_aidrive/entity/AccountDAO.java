package com.kang.kcloud_aidrive.entity;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLRestriction;

/**
 * <p>
 * Account DAO - JPA Entity
 * </p>
 *
 * @author Kai Kang,
 */
@Getter
@Setter
@Table(name = "account")
@Entity
@SQLRestriction("del = 0")  // Automatically filter out deleted records
@Schema(name = "AccountDAO", description = "User Account Table")
public class AccountDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", strategy = "com.kang.kcloud_aidrive.config.SnowflakeConfig")
    private Long id;

    @Schema(description = "Username")
    @Column(name = "username")
    private String username;

    @Schema(description = "Password")
    @Column(name = "password")
    private String password;

    @Schema(description = "Avatar URL")
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Schema(description = "Phone number")
    @Column(name = "phone")
    private String phone;

    @Schema(description = "UserRole - COMMON, ADMIN")
    @Column(name = "role")
    private String role;

    @Schema(description = "Logical Deletion - 1 deleted, 0 not deleted")
    @Column(name = "del", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean del = false;

    @Schema(description = "Created Time - EST")
    @Column(name = "est_create", insertable = false, updatable = false)
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    @Column(name = "est_modified", insertable = false)
    private Date estModified;

    @PrePersist
    public void prePersist() {
        if (del == null) {
            del = false;
        }
    }
}
