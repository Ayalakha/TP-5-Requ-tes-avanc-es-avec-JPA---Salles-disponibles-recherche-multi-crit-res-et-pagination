package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SalleRepository arayüzünün EntityManager tabanlı implementasyonu.
 * JPQL ve Criteria API sorgularını burada topluyoruz.
 */
@Stateless
public class SalleRepositoryImpl implements SalleRepository {

    // Java EE sunucusunda EntityManager otomatik olarak enjekte edilir
    @PersistenceContext(unitName = "SallePU")
    private EntityManager entityManager;

    // Java EE (CDI/EJB) için gerekli parametresiz yapıcı
    public SalleRepositoryImpl() {
    }

    // Java SE testleri için: EntityManager'ı elle enjekte etmeye yarar
    public SalleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * JPQL ile müsait salonları bulur.
     * Bir salon, istenen aralıkla çakışan hiçbir rezervasyonu yoksa müsaittir.
     * İki aralık çakışır  <=>  (aranan başlangıç < rezervasyon bitişi) VE (aranan bitiş > rezervasyon başlangıcı)
     */
    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        // Alt sorgu: aralıkla çakışan rezervasyonu olan salonların id'lerini seçer.
        // Ana sorgu: id'si bu listede OLMAYAN salonları döndürür.
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                      "SELECT r.salle.id FROM Reservation r WHERE (:debut < r.fin AND :fin > r.debut)" +
                      ") ORDER BY s.nom";
        return entityManager.createQuery(jpql, Salle.class)
                .setParameter("debut", debut)   // :debut parametresine değer atanır
                .setParameter("fin", fin)       // :fin parametresine değer atanır
                .getResultList();
    }

    /**
     * Criteria API ile çok kriterli arama + sunucu tarafı sayfalama.
     */
    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);   // FROM Salle s

        // Kriterlere göre koşulları (WHERE) oluştur
        List<Predicate> predicates = buildPredicates(cb, cq, salle, nom, capaciteMin, debut, fin);

        cq.select(salle)
          .where(cb.and(predicates.toArray(new Predicate[0])))
          .orderBy(cb.asc(salle.get("nom")));        // Sayfalamada kararlı sıra için sıralama şart

        TypedQuery<Salle> query = entityManager.createQuery(cq);

        // Sayfalama: page 1'den başlar.
        // setFirstResult -> atlanacak kayıt sayısı (OFFSET), setMaxResults -> sayfa boyutu (LIMIT)
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    /**
     * Aynı kriterlerle toplam kayıt sayısını döndürür (SELECT COUNT).
     */
    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Salle> countRoot = countQuery.from(Salle.class);

        // DİKKAT: tutarsızlığı önlemek için ana sorgudaki AYNI kriterler uygulanır.
        // Koşullar bu sorgunun kendi Root nesnesi üzerinde yeniden oluşturulmalıdır.
        List<Predicate> predicates = buildPredicates(cb, countQuery, countRoot, nom, capaciteMin, debut, fin);

        countQuery.select(cb.count(countRoot))
                  .where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Arama ve sayma sorgularında ortak kullanılan dinamik koşulları oluşturur.
     * Sadece doldurulmuş (null olmayan) kriterler sorguya eklenir.
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, CriteriaQuery<?> query, Root<Salle> salle,
                                            String nom, Integer capaciteMin,
                                            LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> predicates = new ArrayList<>();

        // Ad kriteri: büyük/küçük harf duyarsız "içerir" araması (LIKE %...%)
        if (nom != null && !nom.isEmpty()) {
            predicates.add(cb.like(cb.lower(salle.<String>get("nom")), "%" + nom.toLowerCase() + "%"));
        }

        // Minimum kapasite kriteri: kapasite >= capaciteMin
        if (capaciteMin != null) {
            predicates.add(cb.ge(salle.<Integer>get("capacite"), capaciteMin));
        }

        // Müsaitlik kriteri: aralıkta rezerve edilmiş salonları hariç tut
        if (debut != null && fin != null) {
            // Alt sorgu: çakışan rezervasyonu olan salonların id'leri
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Reservation> reservation = subquery.from(Reservation.class);
            subquery.select(reservation.get("salle").<Long>get("id"));

            // Çakışma koşulu: rezervasyon.debut < fin VE rezervasyon.fin > debut
            Predicate chevauchement = cb.and(
                    cb.lessThan(reservation.<LocalDateTime>get("debut"), fin),
                    cb.greaterThan(reservation.<LocalDateTime>get("fin"), debut)
            );
            subquery.where(chevauchement);

            // salle.id NOT IN (alt sorgu)
            predicates.add(cb.not(salle.get("id").in(subquery)));
        }

        return predicates;
    }
}
