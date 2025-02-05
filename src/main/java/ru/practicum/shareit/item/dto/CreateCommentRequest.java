package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreateCommentRequest {

    @NotBlank(message = "Текст комментария не должен быт пустым")
    private String text;
}
