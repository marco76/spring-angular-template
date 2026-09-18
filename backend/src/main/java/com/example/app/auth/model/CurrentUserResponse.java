package com.example.app.auth.model;

import java.util.List;

public record CurrentUserResponse(String username, List<String> roles) {
}
