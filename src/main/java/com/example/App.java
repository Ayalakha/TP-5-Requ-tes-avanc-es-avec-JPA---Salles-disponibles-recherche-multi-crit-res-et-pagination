package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sorguları ve sayfalamayı test etmek için Java SE ana sınıfı.
 * EntityManagerFactory'yi başlatır, test verisi ekler ve servis metotlarını çalıştırır.
 */
public class App {

    public static void main(String[] args) {
        // "SallePU" kalıcılık biriminden EntityManagerFactory oluştur (persistence.xml)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        try {
            // Test verilerini veritabanına ekle
            initialiserDonnees(em);

            // Elle enjeksiyon: Java EE'de bu işi CDI / EJB otomatik yapar
            SalleService salleService = new SalleServiceImpl(new SalleRepositoryImpl(em));

            // Aranacak zaman aralığı: 1 Haziran 2024, 09:00 - 11:00
            LocalDateTime debut = LocalDateTime.of(2024, 6, 1, 9, 0);
            LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 11, 0);

            // ---- 1) JPQL: müsait salonlar ----
            List<Salle> sallesDisponibles = salleService.getAvailableRooms(debut, fin);
            System.out.println("\n=== Salles disponibles entre " + debut + " et " + fin + " ===");
            for (Salle salle : sallesDisponibles) {
                System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
            }

            // ---- 2) Criteria API: çok kriterli arama ----
            // Adında "salle" geçen, en az 20 kişilik ve aralıkta müsait salonlar
            List<Salle> resultats = salleService.searchRooms("salle", 20, debut, fin, 1, 100);
            System.out.println("\n=== Recherche : nom contient 'salle', capacité >= 20, disponible ===");
            for (Salle salle : resultats) {
                System.out.println("- " + salle);
            }

            // ---- 3) Sayfalama: tüm salonlar, sayfa başına 3 salon ----
            int pageSize = 3;
            int page = 1;
            PaginationResult<Salle> resultat;
            System.out.println("\n=== Pagination (toutes les salles, " + pageSize + " par page) ===");
            do {
                // Kriter yok (null) -> tüm salonlar
                resultat = salleService.searchRoomsPaginated(null, null, null, null, page, pageSize);
                System.out.println("Page " + resultat.getCurrentPage() + "/" + resultat.getTotalPages()
                        + " (total : " + resultat.getTotalItems() + " salles)");
                for (Salle salle : resultat.getItems()) {
                    System.out.println("   - " + salle);
                }
                page++;
            } while (resultat.hasNext());   // Sonraki sayfa varsa devam et

        } finally {
            // Kaynakları her durumda serbest bırak
            em.close();
            emf.close();
        }
    }

    /**
     * Test verilerini ekler: 7 salon ve birkaç rezervasyon.
     * RESOURCE_LOCAL modunda işlem (transaction) elle başlatılıp onaylanır.
     */
    private static void initialiserDonnees(EntityManager em) {
        em.getTransaction().begin();

        Salle a = new Salle("Salle A", 10);
        Salle b = new Salle("Salle B", 25);
        Salle c = new Salle("Salle C", 40);
        Salle d = new Salle("Salle D", 60);
        Salle e = new Salle("Amphi 1", 150);
        Salle f = new Salle("Salle E", 30);
        Salle g = new Salle("Labo Info", 20);

        // Salle B: 08:00-10:00 -> 09:00-11:00 aralığıyla çakışır (müsait DEĞİL)
        b.addReservation(new Reservation(LocalDateTime.of(2024, 6, 1, 8, 0), LocalDateTime.of(2024, 6, 1, 10, 0)));
        // Salle C: 11:00-12:00 -> tam 11:00'de başlar, çakışmaz (müsait)
        c.addReservation(new Reservation(LocalDateTime.of(2024, 6, 1, 11, 0), LocalDateTime.of(2024, 6, 1, 12, 0)));
        // Amphi 1: 09:30-10:30 -> aralığın içinde kalır (müsait DEĞİL)
        e.addReservation(new Reservation(LocalDateTime.of(2024, 6, 1, 9, 30), LocalDateTime.of(2024, 6, 1, 10, 30)));
        // Salle D: başka bir gün -> çakışmaz (müsait)
        d.addReservation(new Reservation(LocalDateTime.of(2024, 6, 2, 9, 0), LocalDateTime.of(2024, 6, 2, 11, 0)));

        // CascadeType.ALL sayesinde rezervasyonlar da salonla birlikte kaydedilir
        for (Salle s : new Salle[]{a, b, c, d, e, f, g}) {
            em.persist(s);
        }

        em.getTransaction().commit();
        // Birinci seviye önbelleği temizle: sorgular doğrudan veritabanından okunsun
        em.clear();
    }
}
