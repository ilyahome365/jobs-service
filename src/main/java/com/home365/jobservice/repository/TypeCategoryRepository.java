package com.home365.jobservice.repository;


import com.home365.jobservice.model.TypeCategoryEntry;
import com.home365.jobservice.model.TypeCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeCategoryRepository extends JpaRepository<TypeCategoryEntry, TypeCategoryId> {

    @Query(value = "select type.Name Type, category.New_name Category, category.New_primaryexpertiseId CategoryId, type.Id TypeId, type.status CategoryType, type.TransferTo\n" +
            "from New_primaryexpertiseExtensionBase category\n" +
            "inner join AccountingTypePrimaryexpertise conn on conn.New_primaryexpertiseExtensionBaseId = category.New_primaryexpertiseId\n" +
            "inner join NewAccountingType type on type.Id = conn.AccountingTypeId order by Type",
            nativeQuery = true)
    List<TypeCategoryEntry> typeCategoryList();

    @Query(value = "select New_name from New_primaryexpertiseExtensionBase category where category.New_primaryexpertiseId = :id", nativeQuery = true)
    String getCategoryNameByID(String id);

    @Query(value = "select name from NewAccountingType type where type.Id = :id", nativeQuery = true)
    String getTypeNameByID(String id);


}

