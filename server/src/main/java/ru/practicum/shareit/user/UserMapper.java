package ru.practicum.shareit.user;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

    UserDto toDto(User user);

    User toUser(CreateUserRequest request);
}
