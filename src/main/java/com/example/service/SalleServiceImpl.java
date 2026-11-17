package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.util.PaginationResult;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SalleService implementasyonu.
 * İş kurallarını (parametre doğrulama vb.) uygular, veri erişimini repository'ye devreder.
 */
@Stateless
public class SalleServiceImpl implements SalleService {

    // Java EE ortamında repository CDI tarafından otomatik enjekte edilir
    @Inject
    private SalleRepository salleRepository;

    // Java EE için gerekli parametresiz yapıcı
    public SalleServiceImpl() {
    }

    // Java SE testleri için: repository'yi elle enjekte etmeye yarar
    public SalleServiceImpl(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    public void setSalleRepository(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        // İş kuralı: aralık geçerli olmalı (başlangıç < bitiş)
        validerCreneau(debut, fin);
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        validerPagination(page, size);
        if (debut != null && fin != null) {
            validerCreneau(debut, fin);
        }
        return salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchRoomsPaginated(String nom, Integer capaciteMin, LocalDateTime debut,
                                                        LocalDateTime fin, int page, int size) {
        // 1) İstenen sayfanın kayıtlarını al
        List<Salle> items = searchRooms(nom, capaciteMin, debut, fin, page, size);
        // 2) Toplam kayıt sayısını al (aynı kriterlerle)
        long total = countRooms(nom, capaciteMin, debut, fin);
        // 3) Hepsini PaginationResult nesnesinde birleştir
        return new PaginationResult<>(items, page, size, total);
    }

    // Zaman aralığını doğrulayan yardımcı metot
    private void validerCreneau(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş tarihleri zorunludur.");
        }
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("Başlangıç tarihi bitiş tarihinden önce olmalıdır.");
        }
    }

    // Sayfalama parametrelerini doğrulayan yardımcı metot
    private void validerPagination(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Sayfa numarası 1 veya daha büyük olmalıdır.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Sayfa boyutu 1 veya daha büyük olmalıdır.");
        }
    }
}
