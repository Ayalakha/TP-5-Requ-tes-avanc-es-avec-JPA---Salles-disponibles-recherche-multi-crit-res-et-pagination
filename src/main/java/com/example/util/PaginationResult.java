package com.example.util;

import java.util.List;

/**
 * Sayfalanmış sonuçlar için genel (generic) yardımcı sınıf.
 * Sayfadaki öğeleri ve gezinme için gerekli bilgileri (meta veriler) birlikte taşır.
 *
 * @param <T> sayfadaki öğelerin tipi (ör. Salle)
 */
public class PaginationResult<T> {

    private final List<T> items;      // Geçerli sayfadaki öğeler
    private final int currentPage;    // Geçerli sayfa numarası (1'den başlar)
    private final int pageSize;       // Sayfa başına öğe sayısı
    private final int totalPages;     // Toplam sayfa sayısı
    private final long totalItems;    // Kriterlere uyan toplam öğe sayısı

    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        // Toplam sayfa = yukarı yuvarlanmış (toplam öğe / sayfa boyutu)
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    // Getter metotları

    public List<T> getItems() { return items; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public long getTotalItems() { return totalItems; }

    // Gezinme için yardımcı metotlar: önceki / sonraki sayfa var mı?
    public boolean hasPrevious() { return currentPage > 1; }
    public boolean hasNext() { return currentPage < totalPages; }
}
