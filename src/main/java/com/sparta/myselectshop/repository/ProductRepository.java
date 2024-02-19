package com.sparta.myselectshop.repository;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByUser(User user, Pageable pageable);


    //Product 엔티티에서 양방향으로 설정했음, 여기서 Folder 가져오려고 하는거임
    //우리가 받아온 폴더 아이디 이용해서 해당하는 폴더에 있는 Product 리스트 가져오자
    Page<Product> findAllByUserAndProductFoldersList_FolderId(User user, Long folderId, Pageable pageable);
}
