package com.devbuild.soutenanceservice.dto;

import com.devbuild.soutenanceservice.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentInfoDTO {
    private String id;
    private DocumentType documentType;
    private String fileName;
    private long fileSize;
}