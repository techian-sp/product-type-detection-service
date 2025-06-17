package com.lilyai.producttypedetection.repository;

import com.lilyai.producttypedetection.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySkuId(String skuId);
    
    List<Product> findByStyleId(String styleId);
    
    List<Product> findBySkuIdIn(Collection<String> skuIds);
    
    @Query("SELECT p FROM Product p WHERE p.skuId IN :skuIds ORDER BY p.id")
    List<Product> findBySkuIdInOrdered(@Param("skuIds") Collection<String> skuIds);
}