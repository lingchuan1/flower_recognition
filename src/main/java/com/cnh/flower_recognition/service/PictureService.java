package com.cnh.flower_recognition.service;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.cnh.flower_recognition.dao.Dao;
import com.cnh.flower_recognition.domain.FloData;
import com.cnh.flower_recognition.domain.ImgDTO;
import com.cnh.flower_recognition.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;

@Service
public class PictureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PictureService.class);

    @Autowired
    private Dao dao;


    public FloData getFloData(Integer id){
        return dao.getFloData(id);
    }


    //图片上传路径
    @Value("${img.save.path}")
    private String imgSavePath;

    public String savePicture(ImgDTO imgDTO){

        if(null != imgDTO){

                //图片压缩数据
                String imgData = imgDTO.getImgData();
                //图片上传名称
                String imgName = imgDTO.getImgName();

                //文件上传成功之后的保存路径
                String imgPath = uploadFile(imgData, imgName);
                LOGGER.info("图片上传路径imgPath：" + imgPath);
                return imgPath;
        }else{
            return null;
        }

    }

    /**
     * 上传图片文件操作
     * @param imgData 图片数据
     * @param imgName 图片名称
     * @return 图片上传路径
     */
    private String uploadFile(String imgData, String imgName){
        //获取当前日期
        String currentDate = DateUtil.getCurrentDate();
        //构建文件上传的路径目录
        String directory = Paths.get(imgSavePath, currentDate).toString();
        File file = new File(directory);
        if(!file.exists()){
            file.mkdirs();
        }

        //加盐，防止文件重名
        int salt = (int) (Math.random()*1000);
        //拼接文件名，构建文件全路径
        String imgPath = Paths.get(directory, imgName+salt).toString();
        file = new File(imgPath);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //使用BASE64Decoder对图片文件数据进行解码操作
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //通过Base64解码，将图片数据解密成字节数组
            byte[] bytes = decoder.decodeBuffer(imgData);

            //构造字节数组输入流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            //读取输入流的数据
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);

            //将数据信息写入图片文件中,不管输出什么格式图片，此处不需改动
            ImageIO.write(bufferedImage, "jpg", file);

            //关闭流
            byteArrayInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgPath;
    }

    public FloData predict(String imgPath) throws MalformedModelException, ModelNotFoundException, IOException, TranslateException {
        FloData floData;
        System.setProperty("ai.djl.repository.zoo.location", "build/pytorch_models/alexnet");
        Translator translator = getTranslator();
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                // only search the model in local directory
                // "ai.djl.localmodelzoo:{name of the model}"
                .optArtifactId("ai.djl.localmodelzoo:alexnet")
                .optTranslator(translator)
                .optProgress(new ProgressBar()).build();

        //ModelZoo代表模型的组合，loadModel方法基于模型的名称获取ModelLoader
        ZooModel model = ModelZoo.loadModel(criteria);

        File fs=new File(imgPath);
        Image img = ImageFactory.getInstance().fromInputStream(new FileInputStream(fs));

        //Predictor提供了用于模型推断的会话，输入是图像数据Image，输出是分类结果Classifications
        Predictor<Image, Classifications> predictor = model.newPredictor();
        Classifications classifications = predictor.predict(img);

        LOGGER.info(classifications.toString());
        floData = getFloData(Integer.valueOf(classifications.topK(1).get(0).getClassName()));
        BigDecimal b = new BigDecimal(classifications.topK(1).get(0).getProbability() * 100);
        floData.setFloConfidence(b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

        return floData;
    }
    private  Translator getTranslator(){
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(224,224,Image.Interpolation.BILINEAR))
                .add(new ToTensor())
                .add(new Normalize(
                        new float[] {0.485f, 0.456f, 0.406f},
                        new float[] {0.229f, 0.224f, 0.225f}));

        Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                .setPipeline(pipeline)
                .optApplySoftmax(true)
                .build();
        return translator;
    }
}
