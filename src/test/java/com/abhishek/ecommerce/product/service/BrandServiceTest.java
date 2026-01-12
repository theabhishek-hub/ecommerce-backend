package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.entity.BrandStatus;
import com.abhishek.ecommerce.product.exception.BrandAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.BrandNotFoundException;
import com.abhishek.ecommerce.product.mapper.BrandMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.service.impl.BrandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand brand;
    private BrandResponseDto brandResponseDto;
    private BrandCreateRequestDto createRequestDto;
    private BrandUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Samsung");
        brand.setDescription("Electronics brand");
        brand.setCountry("South Korea");
        brand.setStatus(BrandStatus.ACTIVE);

        brandResponseDto = new BrandResponseDto();
        brandResponseDto.setId(1L);
        brandResponseDto.setName("Samsung");
        brandResponseDto.setDescription("Electronics brand");
        brandResponseDto.setCountry("South Korea");
        brandResponseDto.setStatus("ACTIVE");

        createRequestDto = new BrandCreateRequestDto();
        createRequestDto.setName("Samsung");
        createRequestDto.setDescription("Electronics brand");
        createRequestDto.setCountry("South Korea");

        updateRequestDto = new BrandUpdateRequestDto();
        updateRequestDto.setName("Updated Samsung");
        updateRequestDto.setDescription("Updated electronics brand");
        updateRequestDto.setCountry("South Korea");
    }

    @Test
    void createBrand_ShouldCreateBrandSuccessfully() {
        // Given
        when(brandRepository.existsByName("Samsung")).thenReturn(false);
        when(brandMapper.toEntity(createRequestDto)).thenReturn(brand);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);
        when(brandMapper.toDto(brand)).thenReturn(brandResponseDto);

        // When
        BrandResponseDto result = brandService.createBrand(createRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Samsung");
        assertThat(result.getDescription()).isEqualTo("Electronics brand");
        assertThat(result.getCountry()).isEqualTo("South Korea");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        verify(brandRepository).existsByName("Samsung");
        verify(brandMapper).toEntity(createRequestDto);
        verify(brandRepository).save(any(Brand.class));
        verify(brandMapper).toDto(brand);
    }

    @Test
    void createBrand_ShouldThrowException_WhenBrandAlreadyExists() {
        // Given
        when(brandRepository.existsByName("Samsung")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> brandService.createBrand(createRequestDto))
                .isInstanceOf(BrandAlreadyExistsException.class)
                .hasMessageContaining("Samsung");

        verify(brandRepository).existsByName("Samsung");
        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    void getBrandById_ShouldReturnBrand() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandMapper.toDto(brand)).thenReturn(brandResponseDto);

        // When
        BrandResponseDto result = brandService.getBrandById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Samsung");

        verify(brandRepository).findById(1L);
        verify(brandMapper).toDto(brand);
    }

    @Test
    void getBrandById_ShouldThrowException_WhenBrandNotFound() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> brandService.getBrandById(1L))
                .isInstanceOf(BrandNotFoundException.class);

        verify(brandRepository).findById(1L);
        verify(brandMapper, never()).toDto(any(Brand.class));
    }

    @Test
    void getAllBrands_ShouldReturnAllBrands() {
        // Given
        List<Brand> brands = List.of(brand);
        when(brandRepository.findAll()).thenReturn(brands);
        when(brandMapper.toDto(any(Brand.class))).thenReturn(brandResponseDto);

        // When
        List<BrandResponseDto> result = brandService.getAllBrands();

        // Then
        assertThat(result).hasSize(1);
        verify(brandRepository).findAll();
        verify(brandMapper).toDto(any(Brand.class));
    }

    @Test
    void getAllActiveBrands_ShouldReturnActiveBrands() {
        // Given
        List<Brand> brands = List.of(brand);
        when(brandRepository.findAllByStatus(BrandStatus.ACTIVE)).thenReturn(brands);
        when(brandMapper.toDto(any(Brand.class))).thenReturn(brandResponseDto);

        // When
        List<BrandResponseDto> result = brandService.getAllActiveBrands();

        // Then
        assertThat(result).hasSize(1);
        verify(brandRepository).findAllByStatus(BrandStatus.ACTIVE);
    }

    @Test
    void updateBrand_ShouldUpdateBrandSuccessfully() {
        // Given
        Brand updatedBrand = new Brand();
        updatedBrand.setId(1L);
        updatedBrand.setName("Updated Samsung");
        updatedBrand.setDescription("Updated electronics brand");
        updatedBrand.setCountry("South Korea");
        updatedBrand.setStatus(BrandStatus.ACTIVE);

        BrandResponseDto updatedResponseDto = new BrandResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Updated Samsung");
        updatedResponseDto.setDescription("Updated electronics brand");
        updatedResponseDto.setCountry("South Korea");
        updatedResponseDto.setStatus("ACTIVE");

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);
        when(brandMapper.toDto(any(Brand.class))).thenReturn(updatedResponseDto);

        // When
        BrandResponseDto result = brandService.updateBrand(1L, updateRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Samsung");

        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(Brand.class));
        verify(brandMapper).toDto(any(Brand.class));
    }

    @Test
    void activateBrand_ShouldActivateBrand() {
        // Given
        Brand inactiveBrand = new Brand();
        inactiveBrand.setId(1L);
        inactiveBrand.setName("Samsung");
        inactiveBrand.setStatus(BrandStatus.INACTIVE);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(inactiveBrand));
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        // When
        brandService.activateBrand(1L);

        // Then
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void deactivateBrand_ShouldDeactivateBrand() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        // When
        brandService.deactivateBrand(1L);

        // Then
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void deleteBrand_ShouldDeleteBrand() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        // When
        brandService.deleteBrand(1L);

        // Then
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(Brand.class));
    }
}