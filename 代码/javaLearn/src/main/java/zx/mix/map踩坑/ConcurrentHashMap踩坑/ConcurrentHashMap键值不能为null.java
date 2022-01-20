package zx.mix.map踩坑.ConcurrentHashMap踩坑;

/**
 * @Description: zx.mix.map踩坑.ConcurrentHashMap踩坑
 * @version: 1.0
 */
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashMap key&value不能为null
 */
public class ConcurrentHashMap键值不能为null {
    public static void main(String[] args) throws MalformedURLException, DocumentException {
        /**
         * 从xml中读取相关配置存到Map中
         * 使用hashMap可以正常存储
         * 使用ConcurrentHashMap存储-- key&value不能为null
         *      即setting标签中，key&value标签缺一不可
         *      不然会报空指针异常
         *
         * 解决：在调用put方法加入元素之前进行判空操作即可
         */
        Map<String,String> setting = new ConcurrentHashMap<>();
        SAXReader reader = new SAXReader();
        File file = new File("src/main/resources/setting/test.xml");
        Document document = reader.read(file);
        Element root = document.getRootElement();// root标签及其子标签
        List<Element> elements = root.elements();// setting标签及其子标签
        for (Element element : elements) {
            String key = element.elementText("key");
            String value = element.elementText("value");
            setting.put(key,value);
        }
        System.out.println(setting.toString());
    }
}
