package ru.practicum.shareit.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String pattern, Object... replaces) {
        super(String.format(pattern, replaces));
    }
}
