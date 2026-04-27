package org.target.dndbackend.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserNameAndEmail {
    private Long id;
    private String username;
    private String email;
}
