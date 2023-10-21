package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.AddressBook;

import java.util.List;

/**
 * <p>
 * 地址簿 服务类
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
public interface IAddressBookService extends IService<AddressBook> {

    /**
     * 新增收货地址
     *
     * @param addressBook
     */
    void addAddress(AddressBook addressBook);

    /**
     * 查询所有地址
     *
     * @return
     */
    List<AddressBook> QueryAllAddresses();

    /**
     * 设置默认地址
     *
     * @param addressBook
     */
    void setDefaultAddress(AddressBook addressBook);

    /**
     *
     */
    AddressBook getDefaultAddress();

    /**
     * 根据id修改地址
     *
     * @param addressBook
     */
    void editAddressById(AddressBook addressBook);

    /**
     * 根据id查询地址
     *
     * @param id
     */
    AddressBook getAddressById(Long id);

    /**
     * 根据id删除地址
     *
     * @param id
     */
    void deleteAddressById(Long id);
}
