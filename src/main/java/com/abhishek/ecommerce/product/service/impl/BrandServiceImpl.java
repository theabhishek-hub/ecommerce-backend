package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.entity.BrandStatus;
import com.abhishek.ecommerce.product.exception.BrandAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.BrandNotFoundException;
import com.abhishek.ecommerce.product.mapper.BrandMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    // ========================= CREATE =========================
    @Override
    public BrandResponseDto createBrand(BrandCreateRequestDto requestDto) {

        // Check duplicate name
        if (brandRepository.existsByName(requestDto.getName())) {
            throw new BrandAlreadyExistsException(
                    "Brand already exists with name: " + requestDto.getName()
            );
        }

        Brand brand = brandMapper.toEntity(requestDto);
        brand.setStatus(BrandStatus.ACTIVE);

        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    // ========================= UPDATE =========================
    @Override
    public BrandResponseDto updateBrand(Long brandId, BrandUpdateRequestDto requestDto) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + brandId));

        // Update only provided fields
        if (requestDto.getName() != null) {
            // Check if name is already taken by another brand
            brandRepository.findByName(requestDto.getName())
                    .ifPresent(existingBrand -> {
                        if (!existingBrand.getId().equals(brandId)) {
                            throw new BrandAlreadyExistsException("Brand already exists with name: " + requestDto.getName());
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
        return brandMapper.toDto(updatedBrand);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(Long brandId) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + brandId));

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
    public List<BrandResponseDto> getAllActiveBrands() {
        return brandRepository.findAllByStatus(BrandStatus.ACTIVE)
                .stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    public void activateBrand(Long brandId) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + brandId));

        brand.setStatus(BrandStatus.ACTIVE);
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void deactivateBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + brandId));

        brand.setStatus(BrandStatus.INACTIVE);
        brandRepository.save(brand);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    @Transactional
    public void deleteBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + brandId));

        brand.setStatus(BrandStatus.INACTIVE);
        brandRepository.save(brand);
    }
}

