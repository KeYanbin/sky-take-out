package com.sky.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@Configuration
public class MyMateObjectHandler implements MetaObjectHandler {
    /**
     * 添加时  自动填充
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("mybatisplus公共字段填充[insert]...");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);//创建时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);//修改时间
        this.strictInsertFill(metaObject, "orderTime", LocalDateTime::now, LocalDateTime.class);//下单时间
        this.strictInsertFill(metaObject, "createUser", Long.class, BaseContext.getCurrentId());//创建人ID
        this.strictInsertFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId());//修改人ID
        this.strictInsertFill(metaObject, "status", Integer.class, 1);

//        metaObject.setValue("createTime", LocalDateTime.now());
//        metaObject.setValue("updateTime", LocalDateTime.now());
//        metaObject.setValue("createUser", BaseContext.getCurrentId());//创建人ID
//        metaObject.setValue("updateUser", BaseContext.getCurrentId());//修改人ID
//        metaObject.setValue("status", StatusConstant.ENABLE);//状态
    }

    /**
     * 更新时 自动填充
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("mybatisplus公共字段填充[update]...");
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);//修改时间
        this.strictInsertFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId());//修改人ID
    }

}
