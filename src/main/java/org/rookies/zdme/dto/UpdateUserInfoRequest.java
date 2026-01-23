package org.rookies.zdme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRequest {
    private String username;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String card_number;
    private Integer admin_lev;
}
