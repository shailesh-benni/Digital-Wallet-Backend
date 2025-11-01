package com.example.demo.dto;

public class UserSummaryDTO {
    private Long id;
    private String name;

    public UserSummaryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
