package ru.practicum.shareit.item;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.practicum.shareit.user.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_text")
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @CreatedDate
    private Instant created;
}
