package ru.practicum.shareit.exception;

public class BadRequest extends RuntimeException {
    public BadRequest(String pattern, Object... replaces) {
        super(String.format(pattern, replaces));
    }
}
