package com.qiu.backend.modules.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@AllArgsConstructor
class UserDTO {
    private Long id;
    private String username;
}


@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        return new UserDTO(id, "Alice");
    }
}
