package org.rookies.zdme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String accessToken;
    private final String refreshToken;
    private final Long userId;

}

