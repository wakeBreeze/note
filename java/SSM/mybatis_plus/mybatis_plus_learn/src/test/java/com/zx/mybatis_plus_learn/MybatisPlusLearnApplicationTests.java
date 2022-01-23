package com.zx.mybatis_plus_learn;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zx.mapper.read.UserMapper;
import com.zx.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

@SpringBootTest
class MybatisPlusLearnApplicationTests {
    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .select("id","name","age")
                .ge("id",3);

        // 参数是一个wrapper，条件构造器
        List<User> users = userMapper.selectList(queryWrapper);
//        Assert.assertEquals(5, userList.size());
        users.forEach(System.out::println);

    }

    // 测试插入
    @Test
    void insertTest(){
        User user = new User();
        user.setName("李四");
        user.setAge(22);
        user.setEmail("386859692@qq.com");

        int i = userMapper.insert(user);
        Consumer c = System.out::println;
        c.accept(i);
    }

    // 测试更新
    @Test
    void updateTest(){
        User user = new User();
        user.setId(1L);
        user.setName("王二");
        user.setAge(999);
        userMapper.updateById(user);
    }
}
