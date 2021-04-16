package com.cnh.flower_recognition;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import com.cnh.flower_recognition.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class FlowerRecognitionApplicationTests {

    @Autowired
    private PictureService pictureService;

    @Test
    void contextLoads(){

    }

}
