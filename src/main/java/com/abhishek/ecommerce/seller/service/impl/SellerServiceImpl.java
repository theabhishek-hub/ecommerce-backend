package com.abhishek.ecommerce.seller.service.impl;

import com.abhishek.ecommerce.seller.dto.response.SellerResponseDto;
import com.abhishek.ecommerce.seller.entity.Seller;
import com.abhishek.ecommerce.seller.exception.SellerNotFoundException;
import com.abhishek.ecommerce.seller.mapper.SellerMapper;
import com.abhishek.ecommerce.seller.repository.SellerRepository;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final SellerMapper sellerMapper;

    @Override
    public SellerResponseDto applyForSeller(Long userId) {
        log.info("User {} applying to become seller", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user already has a seller profile
        if (sellerRepository.existsByUserId(userId)) {
            Seller existingSeller = sellerRepository.findByUserId(userId)
                    .orElseThrow(() -> new SellerNotFoundException("Seller profile not found for user: " + userId));

            if (existingSeller.getStatus() == SellerStatus.REQUESTED) {
                log.warn("User {} already has a REQUESTED seller application", userId);
                throw new IllegalStateException("Seller application already pending for this user");
            }
            if (existingSeller.getStatus() == SellerStatus.APPROVED) {
                log.warn("User {} is already an approved seller", userId);
                throw new IllegalStateException("User is already an approved seller");
            }
        }

        // Create new seller profile
        Seller seller = new Seller();
        seller.setUser(user);
        seller.setStatus(SellerStatus.REQUESTED);
        seller = sellerRepository.save(seller);

        log.info("Seller application created for user {} with ID {}", userId, seller.getId());
        return sellerMapper.toDto(seller);
    }

    @Override
    public SellerResponseDto approveSeller(Long sellerId, Long adminUserId) {
        log.info("Admin {} approving seller {}", adminUserId, sellerId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException(sellerId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException(adminUserId));

        // Validate admin has ROLE_ADMIN
        if (!admin.getRoles().contains(Role.ROLE_ADMIN)) {
            log.warn("Non-admin user {} attempted to approve seller", adminUserId);
            throw new IllegalStateException("Only admins can approve sellers");
        }

        seller.setStatus(SellerStatus.APPROVED);
        seller.setApprovedAt(LocalDateTime.now());
        seller.setApprovedByAdmin(admin);

        // Assign ROLE_SELLER to the user if not already present
        User sellerUser = seller.getUser();
        if (!sellerUser.getRoles().contains(Role.ROLE_SELLER)) {
            sellerUser.getRoles().add(Role.ROLE_SELLER);
            userRepository.save(sellerUser);
            log.info("Assigned ROLE_SELLER to user {}", sellerUser.getId());
        }

        seller = sellerRepository.save(seller);
        log.info("Seller {} approved by admin {}", sellerId, adminUserId);
        return sellerMapper.toDto(seller);
    }

    @Override
    public SellerResponseDto rejectSeller(Long sellerId, Long adminUserId, String rejectionReason) {
        log.info("Admin {} rejecting seller {}", adminUserId, sellerId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException(sellerId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException(adminUserId));

        // Validate admin has ROLE_ADMIN
        if (!admin.getRoles().contains(Role.ROLE_ADMIN)) {
            log.warn("Non-admin user {} attempted to reject seller", adminUserId);
            throw new IllegalStateException("Only admins can reject sellers");
        }

        seller.setStatus(SellerStatus.REJECTED);
        seller.setApprovedAt(LocalDateTime.now());
        seller.setApprovedByAdmin(admin);
        seller.setRejectionReason(rejectionReason);

        // Remove ROLE_SELLER from the user if present
        User sellerUser = seller.getUser();
        if (sellerUser.getRoles().contains(Role.ROLE_SELLER)) {
            sellerUser.getRoles().remove(Role.ROLE_SELLER);
            userRepository.save(sellerUser);
            log.info("Removed ROLE_SELLER from user {}", sellerUser.getId());
        }

        seller = sellerRepository.save(seller);
        log.info("Seller {} rejected by admin {}", sellerId, adminUserId);
        return sellerMapper.toDto(seller);
    }

    @Override
    public SellerResponseDto suspendSeller(Long sellerId, Long adminUserId, String suspensionReason) {
        log.info("Admin {} suspending seller {}", adminUserId, sellerId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException(sellerId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException(adminUserId));

        // Validate admin has ROLE_ADMIN
        if (!admin.getRoles().contains(Role.ROLE_ADMIN)) {
            log.warn("Non-admin user {} attempted to suspend seller", adminUserId);
            throw new IllegalStateException("Only admins can suspend sellers");
        }

        seller.setStatus(SellerStatus.SUSPENDED);
        seller.setRejectionReason(suspensionReason); // Reuse field for suspension reason

        // Remove ROLE_SELLER from the user
        User sellerUser = seller.getUser();
        if (sellerUser.getRoles().contains(Role.ROLE_SELLER)) {
            sellerUser.getRoles().remove(Role.ROLE_SELLER);
            userRepository.save(sellerUser);
            log.info("Removed ROLE_SELLER from user {}", sellerUser.getId());
        }

        seller = sellerRepository.save(seller);
        log.info("Seller {} suspended by admin {}", sellerId, adminUserId);
        return sellerMapper.toDto(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerResponseDto getSellerById(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException(sellerId));
        return sellerMapper.toDto(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerResponseDto getSellerByUserId(Long userId) {
        return sellerRepository.findByUserId(userId)
                .map(sellerMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> getAllPendingSellers() {
        return sellerRepository.findByStatus(SellerStatus.REQUESTED)
                .stream()
                .map(sellerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> getSellersByStatus(SellerStatus status) {
        return sellerRepository.findByStatus(status)
                .stream()
                .map(sellerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApprovedSeller(Long userId) {
        return sellerRepository.findByUserId(userId)
                .map(seller -> seller.getStatus() == SellerStatus.APPROVED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSellerApplicant(Long userId) {
        return sellerRepository.existsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(SellerStatus status) {
        return sellerRepository.countByStatus(status);
    }

}
