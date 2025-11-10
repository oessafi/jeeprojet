package com.devbuild.inscriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfoDTO {
    private String id;
    private String fileName;
    private String contentType;
    private long fileSize;
}