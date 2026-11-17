package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Rezervasyon varlığı: bir salonun belirli bir zaman aralığında ayrılmasını temsil eder.
 * "reservations" tablosuna eşlenir.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    // Birincil anahtar, otomatik artırılır
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Çoka-bir ilişki: birçok rezervasyon aynı salona ait olabilir.
    // salle_id yabancı anahtar (foreign key) sütunudur.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    // Rezervasyonun başlangıç tarihi ve saati
    @Column(nullable = false)
    private LocalDateTime debut;

    // Rezervasyonun bitiş tarihi ve saati
    @Column(nullable = false)
    private LocalDateTime fin;

    // JPA için zorunlu parametresiz yapıcı
    public Reservation() {
    }

    // Kolay oluşturma için yardımcı yapıcı
    public Reservation(LocalDateTime debut, LocalDateTime fin) {
        this.debut = debut;
        this.fin = fin;
    }

    // Getter ve setter metotları

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public LocalDateTime getDebut() { return debut; }
    public void setDebut(LocalDateTime debut) { this.debut = debut; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
}
