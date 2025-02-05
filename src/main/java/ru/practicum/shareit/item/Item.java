package ru.practicum.shareit.item;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.itemRequest.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;

    private String description;

    private boolean available;

    @OneToMany(mappedBy = "item")
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest itemRequest;

    @Transient
    private LocalDateTime lastBooking;

    @Transient
    private LocalDateTime nextBooking;

    public Item(Long id, User owner, String name, String description, boolean available, List<Comment> comments) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.available = available;
        if (comments != null) {
            this.comments = comments;
        }
    }
}
