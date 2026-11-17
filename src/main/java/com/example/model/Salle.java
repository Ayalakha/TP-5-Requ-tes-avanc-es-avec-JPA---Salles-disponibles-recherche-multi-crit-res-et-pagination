package com.example.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Salon varlığı (entity): adı ve kapasitesi olan bir toplantı salonunu temsil eder.
 * "salles" tablosuna eşlenir.
 */
@Entity
@Table(name = "salles")
public class Salle {

    // Birincil anahtar, veritabanı tarafından otomatik artırılır
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Salon adı: zorunlu ve benzersiz
    @Column(nullable = false, unique = true)
    private String nom;

    // Salonun kişi kapasitesi: zorunlu
    @Column(nullable = false)
    private int capacite;

    // Bir salonun birden fazla rezervasyonu olabilir (bire-çok ilişki).
    // mappedBy: ilişkinin sahibi Reservation.salle alanıdır.
    // LAZY: rezervasyonlar sadece ihtiyaç duyulduğunda yüklenir.
    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    // JPA için zorunlu parametresiz yapıcı
    public Salle() {
    }

    // Kolay oluşturma için yardımcı yapıcı
    public Salle(String nom, int capacite) {
        this.nom = nom;
        this.capacite = capacite;
    }

    // Getter ve setter metotları

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public Set<Reservation> getReservations() { return reservations; }
    public void setReservations(Set<Reservation> reservations) { this.reservations = reservations; }

    // İlişkinin iki tarafını da tutarlı tutan yardımcı metot
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setSalle(this);
    }

    @Override
    public String toString() {
        return nom + " (Capacité: " + capacite + ")";
    }
}
