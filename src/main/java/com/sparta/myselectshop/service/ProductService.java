package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.exception.ProductNotFoundException;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.NullServiceException;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FolderRepository folderRepository;
    private final ProductFolderRepository productFolderRepository;
    private final MessageSource messageSource;

    public static final int MIN_MY_PRICE=100;

    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        Product product = productRepository.save(new Product(requestDto, user));
        return new ProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice=requestDto.getMyprice();
        if(myprice<MIN_MY_PRICE){
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            "below.min.my.price",
                            //메시지 내에서 매개변수로 전달할 경우 전달할 값
                            //{}안 값
                            new Integer[]{MIN_MY_PRICE},
                            //만약 code값 못찾으면 이거 출력
                            "Wrong Price",
                            //기본 언어설정 우리 서비스가 국제화 될 때 한국어로 표시 => 해당 언어 ex)영어로 번역
                            Locale.getDefault()

                    )
            );
        }

        Product product=productRepository.findById(id).orElseThrow(()->
                new ProductNotFoundException(messageSource.getMessage(
                        "not.found.product",
                        null,
                        "Not Found Product",
                        Locale.getDefault()
                )));

        product.update(requestDto);
        return new ProductResponseDto(product);
    }

    //얘 지연로딩 씀 = 영속성 컨텍스트로 관리되어야 함 = 트랜잭션 환경 필요함
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction=isAsc?Sort.Direction.ASC:Sort.Direction.DESC;
        Sort sort=Sort.by(direction, sortBy);
        Pageable pageable= PageRequest.of(page,size,sort);

        UserRoleEnum userRoleEnum=user.getRole();
        Page<Product> productList;

        if(userRoleEnum == UserRoleEnum.USER){
            productList=productRepository.findAllByUser(user,pageable);
        }else{
            productList=productRepository.findAll(pageable);
        }

        return productList.map(ProductResponseDto::new);
    }

    //Page안에 map보면 잘바꿔주는거있음bb
    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(()->
                new NullPointerException("해당 상품은 존재하지 않습니다."));
        product.updateByItemDto(itemDto);
    }

    //상품에 폴더 추가
    public void addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(()->
                new NullPointerException("해당 상품이 존재하지 않습니다"));
        Folder folder=folderRepository.findById(folderId).orElseThrow(()->
                new NullPointerException("해당 폴더가 존재하지 않습니다."));

        //현재 로그인한 유저가 등록한 폴더 상품이 맞는지 확인해야함
        //만약에 해당하지 않으면
        if(!product.getUser().getId().equals(user.getId())
        || !folder.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        //해당 폴더에 이미 상품이 등록되어 있는 경우 -> 중복 확인
        Optional<ProductFolder> overlapFolder =  productFolderRepository.findByProductAndFolder(product, folder);
        if(overlapFolder.isPresent()){
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }

        productFolderRepository.save(new ProductFolder(product,folder));
    }

    public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        //페이징 처리
        Sort.Direction direction=isAsc?Sort.Direction.ASC:Sort.Direction.DESC;
        Sort sort=Sort.by(direction, sortBy);
        Pageable pageable= PageRequest.of(page,size,sort);

        Page<Product> productList = productRepository.findAllByUserAndProductFoldersList_FolderId(user, folderId, pageable);

        //받아왔으면 변환하자
        Page<ProductResponseDto> responseDtoList = productList.map(ProductResponseDto::new);
        return responseDtoList;
    }
}
