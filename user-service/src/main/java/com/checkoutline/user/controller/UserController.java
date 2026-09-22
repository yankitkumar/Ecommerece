package com.checkoutline.user.controller;

import com.checkoutline.user.dto.AddressRequest;
import com.checkoutline.user.model.Address;
import com.checkoutline.user.model.User;
import com.checkoutline.user.repository.AddressRepository;
import com.checkoutline.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserController(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/users/me")
    public User me(Authentication authentication) {
        return userRepository.findById(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/users/me/addresses")
    public List<Address> myAddresses(Authentication authentication) {
        return addressRepository.findByUserId(authentication.getName());
    }

    /** POST: a new address doesn't have an id yet, and adding one isn't idempotent. */
    @PostMapping("/users/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public Address addAddress(Authentication authentication, @Valid @RequestBody AddressRequest request) {
        Address address = new Address(authentication.getName(), request.line1(), request.city(),
                request.province(), request.postalCode(), request.country(), request.isDefault());
        return addressRepository.save(address);
    }

    /**
     * PUT, not POST: the client names the resource (an id it already has) and supplies the
     * full replacement body — repeating the exact same call leaves the same end state, which
     * is what makes PUT safe to retry where POST /orders is not (see order-service).
     */
    @PutMapping("/users/me/addresses/{addressId}")
    public Address replaceAddress(Authentication authentication, @PathVariable String addressId, @Valid @RequestBody AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No address " + addressId));
        if (!address.getUserId().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your address");
        }
        address.replace(request.line1(), request.city(), request.province(), request.postalCode(), request.country(), request.isDefault());
        return addressRepository.save(address);
    }
}
