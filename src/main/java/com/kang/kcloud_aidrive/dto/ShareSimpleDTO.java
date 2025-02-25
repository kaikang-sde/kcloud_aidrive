package com.kang.kcloud_aidrive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Kai Kang
 */
@Getter
@Setter
@Schema(name = "ShareSimpleDO", description = "分享链接简单对象")
@Accessors(chain = true)
public class ShareSimpleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分享id")
    private Long id;

    @Schema(description = "分享名称")
    private String shareName;

    @Schema(description = "分享类型（no_code没有提取码 ,need_code有提取码）")
    private String shareType;

    @Schema(description = "分享类型（0 永久有效；1: 7天有效；2: 30天有效）")
    private Integer shareDayType;

    @Schema(description = "分享有效天数（永久有效为0）")
    private Integer shareDay;

    @Schema(description = "分享结束时间")
    private Date shareEndTime;

    @Schema(description = "分享链接地址")
    private String shareUrl;

    @Schema(description = "分享者信息")
    private ShareAccountDTO shareAccountDTO;

    @Schema(description = "分享token, 不需要校验码时使用")
    private String shareToken;

}
