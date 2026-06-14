package org.target.dndbackend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgetPasswordRequest {
    private String verificationCode;
    private String newPassword;
}
