package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.shared.enums.BrandStatus;
import com.abhishek.ecommerce.product.exception.BrandAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.BrandNotFoundException;
import com.abhishek.ecommerce.product.mapper.BrandMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    // ========================= CREATE =========================
    @Override
    @CacheEvict(value = "brands", allEntries = true)
    public BrandResponseDto createBrand(BrandCreateRequestDto requestDto) {
        log.info("createBrand started for name={}", requestDto.getName());

        // Check duplicate name
        if (brandRepository.existsByName(requestDto.getName())) {
            log.warn("createBrand duplicate name={}", requestDto.getName());
            throw new BrandAlreadyExistsException(requestDto.getName());
        }

        Brand brand = brandMapper.toEntity(requestDto);
        brand.setStatus(BrandStatus.ACTIVE);

        Brand savedBrand = brandRepository.save(brand);
        log.info("createBrand completed brandId={} name={}", savedBrand.getId(), requestDto.getName());
        return brandMapper.toDto(savedBrand);
    }

    // ========================= UPDATE =========================
    @Override
    @CacheEvict(value = "brands", allEntries = true)
    public BrandResponseDto updateBrand(Long brandId, BrandUpdateRequestDto requestDto) {
        log.info("updateBrand started for brandId={}", brandId);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException(brandId));

        // Update only provided fields
        if (requestDto.getName() != null) {
            // Check if name is already taken by another brand
            brandRepository.findByName(requestDto.getName())
                    .ifPresent(existingBrand -> {
                        if (!existingBrand.getId().equals(brandId)) {
                            log.warn("updateBrand duplicate name={} brandId={}", requestDto.getName(), brandId);
                            throw new BrandAlreadyExistsException(requestDto.getName());
                        }
                    });
            brand.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            brand.setDescription(requestDto.getDescription());
        }
        if (requestDto.getCountry() != null) {
            brand.setCountry(requestDto.getCountry());
        }

        Brand updatedBrand = brandRepository.save(brand);
        log.info("updateBrand completed brandId={}", brandId);
        return brandMapper.toDto(updatedBrand);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands", key = "#brandId")
    public BrandResponseDto getBrandById(Long brandId) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException(brandId));

        return brandMapper.toDto(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands", key = "'active'")
    public List<BrandResponseDto> getAllActiveBrands() {
        return brandRepository.findAllByStatus(BrandStatus.ACTIVE)
                .stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void activateBrand(Long brandId) {
        log.info("activateBrand started for brandId={}", brandId);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException(brandId));

        brand.setStatus(BrandStatus.ACTIVE);
        brandRepository.save(brand);
        log.info("activateBrand completed brandId={}", brandId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deactivateBrand(Long brandId) {
        log.info("deactivateBrand started for brandId={}", brandId);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException(brandId));

        brand.setStatus(BrandStatus.INACTIVE);
        brandRepository.save(brand);
        log.info("deactivateBrand completed brandId={}", brandId);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deleteBrand(Long brandId) {
        log.info("deleteBrand started for brandId={}", brandId);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException(brandId));

        brand.setStatus(BrandStatus.INACTIVE);
        brandRepository.save(brand);
        log.info("deleteBrand completed brandId={}", brandId);
    }
}

