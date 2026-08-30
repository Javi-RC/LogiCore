package com.logicore.userservice.application.port.out;

import com.logicore.userservice.application.model.AuthenticatedUser;

public interface TokenProvider {

    String issue(AuthenticatedUser user, long ttlMillis);
}