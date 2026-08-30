package com.gomovie.screen;

import com.gomovie.common.entity.BaseEntity;
import com.gomovie.theatre.Theatre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "screen",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_screen_theatre_name",
                columnNames = {"theatre_id", "name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Screen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "theatre_id",
            nullable = false
    )
    private Theatre theatre;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}