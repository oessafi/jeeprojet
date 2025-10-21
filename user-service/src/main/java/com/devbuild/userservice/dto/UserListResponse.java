package com.devbuild.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    private boolean success;
    private String message;
    private java.util.List<UserDTO> data;
    private int total;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public static UserListResponse success(java.util.List<UserDTO> data, String message) {
        return UserListResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .total(data.size())
                .timestamp(LocalDateTime.now())
                .build();
    }
}