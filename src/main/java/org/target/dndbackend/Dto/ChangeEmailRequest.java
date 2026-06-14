package org.target.dndbackend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeEmailRequest {
    private String newEmail;
    private String verificationCode;
}
