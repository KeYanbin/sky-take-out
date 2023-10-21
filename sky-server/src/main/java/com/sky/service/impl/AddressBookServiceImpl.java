package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.IAddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 地址簿 服务实现类
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {

    /**
     * 新增收货地址
     *
     * @param addressBook
     */
    @Override
    public void addAddress(AddressBook addressBook) {
        // 获取用户id
        addressBook.setUserId(BaseContext.getCurrentId());

        save(addressBook);
    }

    /**
     * 查询所有地址
     *
     * @return
     */
    @Override
    public List<AddressBook> QueryAllAddresses() {
        return lambdaQuery().eq(AddressBook::getUserId, BaseContext.getCurrentId()).list();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     */
    @Override
    @Transactional
    public void setDefaultAddress(AddressBook addressBook) {
        // 查询原先的默认地址，并取消默认地址
        lambdaUpdate()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1)
                .set(AddressBook::getIsDefault, 0)
                .update();
        // 设置新的默认地址
        addressBook.setIsDefault(1);
        updateById(addressBook);
    }

    /**
     * 查询默认地址
     */
    @Override
    public AddressBook getDefaultAddress() {
        return lambdaQuery()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1)
                .one();
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook
     */
    @Override
    public void editAddressById(AddressBook addressBook) {
        updateById(addressBook);
    }

    @Override
    public AddressBook getAddressById(Long id) {
        return getById(id);
    }

    @Override
    public void deleteAddressById(Long id) {
        removeById(id);
    }


}
