package com.kang.kcloud_aidrive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kai Kang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadUrlDTO {
    private String fileName;
    private String downloadUrl;

}
