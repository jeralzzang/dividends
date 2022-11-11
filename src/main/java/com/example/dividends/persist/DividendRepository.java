package com.example.dividends.persist;

import com.example.dividends.persist.entity.DividendEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity,Long> {
  List<DividendEntity> findAllByCompanyId(Long companyId);
  boolean existsByCompanyIdAndDate(Long companyId, LocalDateTime date);

  @Transactional
  void deleteByCompanyId(Long id);
}
