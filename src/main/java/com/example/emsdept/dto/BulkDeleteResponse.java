package com.example.emsdept.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class BulkDeleteResponse {

    private List<Long> deletedIds;

    private int deletedCount;

    private String message;
}