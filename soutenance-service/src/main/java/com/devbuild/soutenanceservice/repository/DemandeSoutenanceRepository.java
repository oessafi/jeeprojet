package com.devbuild.soutenanceservice.repository;

import com.devbuild.soutenanceservice.model.DemandeSoutenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeSoutenanceRepository extends JpaRepository<DemandeSoutenance, String> {
    List<DemandeSoutenance> findByDoctorantId(String doctorantId);
}