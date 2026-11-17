package com.example.repository;

import com.example.model.Salle;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Salon verilerine erişim arayüzü (Repository).
 * Veritabanı sorgularını iş mantığından ayırır.
 */
public interface SalleRepository {

    // Verilen zaman aralığında hiç rezervasyonu olmayan salonları döndürür (JPQL)
    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    // Çok kriterli dinamik arama + sayfalama (Criteria API).
    // page 1'den başlar; null olan kriterler dikkate alınmaz.
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    // Aynı kriterlere uyan toplam salon sayısı (sayfa sayısını hesaplamak için)
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);
}
