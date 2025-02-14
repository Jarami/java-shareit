package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookingRequest {
	@NotNull
	private Long itemId;

	@NotNull
	@Future
	private LocalDateTime start;

	@NotNull
	@Future
	private LocalDateTime end;
}
