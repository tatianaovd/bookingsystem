package com.demo.bookingsystem.repository;

import com.demo.bookingsystem.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUnitId(Long unitId);
}
