package com.kang.kcloud_aidrive.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户分享表
 * </p>
 *
 * @author Kai Kang
 */
@Getter
@Setter
@Entity
@Table(name = "share")
@Schema(name = "ShareDAO", description = "用户分享表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分享id")
    @Id
    private Long id;

    @Schema(description = "分享名称")
    @Column(name = "share_name")
    private String shareName;

    @Schema(description = "分享类型（no_code没有提取码 ,need_code有提取码）")
    @Column(name = "share_type")
    private String shareType;

    @Schema(description = "分享类型（0 永久有效；1: 7天有效；2: 30天有效）")
    @Column(name = "share_day_type")
    private Integer shareDayType;

    @Schema(description = "分享有效天数（永久有效为0）")
    @Column(name = "share_day")
    private Integer shareDay;

    @Schema(description = "分享结束时间")
    @Column(name = "share_end_time")
    private Date shareEndTime;

    @Schema(description = "分享链接地址")
    @Column(name = "share_url")
    private String shareUrl;

    @Schema(description = "分享提取码")
    @Column(name = "share_code")
    private String shareCode;

    @Schema(description = "分享状态  used正常, expired已失效,  cancled取消")
    @Column(name = "share_status")
    private String shareStatus;

    @Schema(description = "分享创建人")
    @Column(name = "account_id")
    private Long accountId;

    @Schema(description = "Created Time - EST")
    @Column(name = "est_create", insertable = false, updatable = false)
    private Date estCreate;

    @Schema(description = "Modified Time - EST")
    @Column(name = "est_modified", insertable = false)
    private Date estModified;
}
