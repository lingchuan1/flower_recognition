package com.cnh.flower_recognition.controller;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import com.cnh.flower_recognition.domain.FloData;
import com.cnh.flower_recognition.domain.ImgDTO;
import com.cnh.flower_recognition.result.CodeMsg;
import com.cnh.flower_recognition.result.Result;
import com.cnh.flower_recognition.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @GetMapping(value = "/")
    public String getIndex() {
        return "index";
    }

    @ResponseBody
    @PostMapping(value = "/picture/upload")
    public Result<FloData> upload(@RequestBody ImgDTO imgDTO) throws MalformedModelException, ModelNotFoundException, TranslateException, IOException {
        String imgPath = pictureService.savePicture(imgDTO);
        if (imgPath == null)
            return Result.error(CodeMsg.SERVER_ERROR);

        FloData floData = pictureService.predict(imgPath);
        return Result.success(floData);
    }
}
