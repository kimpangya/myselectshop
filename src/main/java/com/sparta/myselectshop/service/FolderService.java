package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;

    public void addFolders(List<String> folderNames, User user) {
        //회원이 새로 생성한 폴더 이름들 vs 기존 폴더 이름들 확인하고 중복인지 보기
        //끝에 In붙이는거 = 여러개 FolderNames 넣을거잖음 여러개로 검색하려면 in 붙여줘야함
        List<Folder> existFolderList= folderRepository.findAllByUserAndNameIn(user, folderNames);
        List<Folder> folderList=new ArrayList<>();

        for (String folderName : folderNames) {
            if(!isExistFolderName(folderName, existFolderList)){
                Folder folder = new Folder(folderName, user);
                folderList.add(folder);
            }else{
                throw new IllegalArgumentException("중복된 폴더명을 제거해주세요! 폴더명 : "+folderName);
            }
        }

        folderRepository.saveAll(folderList);
    }

    public List<FolderResponseDto> getFolders(User user) {
        List<Folder> folderList=folderRepository.findAllByUser(user);
        List<FolderResponseDto> responseDtoList = new ArrayList<>();

        for (Folder folder : folderList) {
            responseDtoList.add(new FolderResponseDto(folder));
        }
        return responseDtoList;
    }

    private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
        for (Folder existfolder : existFolderList) {
            if(folderName.equals(existfolder.getName())){
                return true;
            }
        }
        return false;
    }
}
