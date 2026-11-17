package com.example.service;

import com.example.model.Salle;
import com.example.util.PaginationResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Salon iş servisi arayüzü (iş katmanı).
 * Repository'yi çağırır ve gerekirse iş kurallarını uygular.
 */
public interface SalleService {

    // Belirtilen aralıkta müsait salonlar
    List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    // Çok kriterli arama (tek bir sayfa)
    List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size);

    // Kriterlere uyan toplam salon sayısı
    long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin);

    // Arama sonucu + sayfalama bilgileri (toplam sayfa, toplam kayıt...) tek nesnede
    PaginationResult<Salle> searchRoomsPaginated(String nom, Integer capaciteMin, LocalDateTime debut,
                                                 LocalDateTime fin, int page, int size);
}
