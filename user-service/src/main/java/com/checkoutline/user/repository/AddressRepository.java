package com.checkoutline.user.repository;

import com.checkoutline.user.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByUserId(String userId);
}
