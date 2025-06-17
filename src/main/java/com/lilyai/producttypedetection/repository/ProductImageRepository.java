package com.lilyai.producttypedetection.repository;

import com.lilyai.producttypedetection.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductId(Long productId);
    
    Page<ProductImage> findByProductId(Long productId, Pageable pageable);
    
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);
    
    List<ProductImage> findByProductIdAndIsActiveTrue(Long productId);
    
    List<ProductImage> findByImageUrlContaining(String urlPattern);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.productId = :productId AND pi.isActive = true ORDER BY pi.isPrimary DESC, pi.sortOrder ASC")
    List<ProductImage> findActiveImagesByProductIdOrderedBySortOrder(@Param("productId") Long productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.createdAt BETWEEN :startDate AND :endDate")
    List<ProductImage> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.productId = :productId AND pi.isActive = true")
    long countActiveImagesByProductId(@Param("productId") Long productId);
    
    void deleteByProductId(Long productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.isActive = false AND pi.updatedAt < :cutoffDate")
    List<ProductImage> findInactiveImagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    boolean existsByProductIdAndImageUrl(Long productId, String imageUrl);
    
    @Query("SELECT DISTINCT pi.productId FROM ProductImage pi WHERE pi.isActive = true")
    List<Long> findDistinctProductIdsWithActiveImages();
}