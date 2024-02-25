package com.sparta.myselectshop.exception;


//runtimeException상속받으면 번개모양 됨
public class ProductNotFoundException extends RuntimeException{
    //ctrl+o
    public ProductNotFoundException(String message) {
        super(message);
    }
}
