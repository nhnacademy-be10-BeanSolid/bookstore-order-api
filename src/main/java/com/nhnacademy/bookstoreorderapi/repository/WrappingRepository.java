package com.nhnacademy.bookstoreorderapi.repository;

import com.nhnacademy.bookstoreorderapi.entity.Wrapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WrappingRepository extends JpaRepository<Wrapping, Long> {
}
