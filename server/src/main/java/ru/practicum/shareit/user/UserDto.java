package ru.practicum.shareit.user;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;
    private String name;
    private String email;
}
