package com.zx.service.read.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zx.mapper.read.Ka92AndKa94ReadMapper;
import com.zx.mapper.write.Ka92WriteMapper;
import com.zx.mapper.write.Ka94WriteMapper;
import com.zx.pojo.Ka92;
import com.zx.pojo.Ka92AndKa94;
import com.zx.pojo.Ka94;
import com.zx.service.read.Ka92AndKa94ReadService;
import com.zx.service.write.Ka92WriteService;
import com.zx.service.write.Ka94WriteService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description: com.zx.service.read.impl
 * @version: 1.0
 */
@Service
public class Ka92AndKa94ReadServiceImpl implements Ka92AndKa94ReadService {
    AtomicLong atomic_aaz328 = new AtomicLong(3213000001309321l);
    AtomicLong atomic_aaz356 = new AtomicLong(3213000205623841l);

    @Resource
    private Ka92AndKa94ReadMapper ka92AndKa94ReadMapper;
    @Resource
    private Ka92WriteService ka92WriteService;
    @Resource
    private Ka94WriteService ka94WriteService;

    @Resource
    private Ka92WriteMapper ka92WriteMapper;
    @Resource
    private Ka94WriteMapper ka94WriteMapper;

    @Override
    public void getKa92AndKa94s() {
        List<Ka92AndKa94> ka92AndKa94s = ka92AndKa94ReadMapper.getKa92AndKa94s();

        List<Ka92> ka92s = new ArrayList<>();
        List<Ka94> ka94s = new ArrayList<>();

        /*ka92AndKa94s.parallelStream().forEach(ka92AndKa94->{

        });*/
        for (Ka92AndKa94 ka92AndKa94 : ka92AndKa94s) {
            String aaz328 = String.valueOf(atomic_aaz328.addAndGet(1l));
            String aaz356 = String.valueOf(atomic_aaz356.addAndGet(1l));
//            System.out.println(aaz328+"--"+aaz356);
            /**ka92*/
            Ka92 ka92 = new Ka92();
            ka92.setAaz328(aaz328);
            ka92.setAaz319(ka92AndKa94.getAaz319());
            ka92.setApe800(ka92AndKa94.getApe800());
            ka92.setAze001(ka92AndKa94.getAze001());
            ka92.setAaz217(ka92AndKa94.getAaz217());
            ka92.setAae317(ka92AndKa94.getAae317());
            ka92.setAaa027(ka92AndKa94.getAaa027());
            ka92.setAab034(ka92AndKa94.getAab034());

            /**ka94*/
            Ka94 ka94 = new Ka94();
            ka94.setAaz356(aaz356);
            ka94.setAaz328(aaz328);
            ka94.setAkb020(ka92AndKa94.getAkb020());
            ka94.setAaz217(ka92AndKa94.getAaz217());
            ka94.setAaz308(ka92AndKa94.getAaz308());
            ka94.setAaz213(ka92AndKa94.getAaz213());
            ka94.setApe835(ka92AndKa94.getApe835());
            ka94.setApe801(ka92AndKa94.getApe801());
            ka94.setApe805(ka92AndKa94.getApe805());
            ka94.setApe802(ka92AndKa94.getApe802());
            ka94.setAaz319(ka92AndKa94.getAaz319());
            ka94.setAaa027(ka92AndKa94.getAaa027());

            ka92s.add(ka92);
            ka94s.add(ka94);
//            ka92WriteMapper.insert(ka92);
//            ka94WriteMapper.insert(ka94);

        }
        ka92WriteService.saveBatch(ka92s,3000);
        ka94WriteService.saveBatch(ka94s,3000);
        System.out.println("插入完毕！！！");
    }
}
