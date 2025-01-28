package com.kang.kcloud_aidrive.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FolderTreeNodeDTO {
    private Long id;
    private Long parentId;
    // file name
    private String label;

    private List<FolderTreeNodeDTO> children = new ArrayList<>();
}
