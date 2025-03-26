package com.demo.bookingsystem.repository;

import com.demo.bookingsystem.domain.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long>, JpaSpecificationExecutor<Unit> {

    @Query("SELECT u.id FROM Unit u")
    List<Long> findAllUnitIds();
}
