package com.sky.controller.user;


import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.IAddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 地址簿 前端控制器
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
@Slf4j
@RestController
@Api(tags = "小程序-地址簿接口")
@RequestMapping("/user/addressBook")
public class AddressBookController {

    @Autowired
    private IAddressBookService addressBookService;

    /**
     * 新增收货地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增收货地址")
    public Result addAddress(@RequestBody AddressBook addressBook) {
        log.info("## 新增地址: {}", addressBook);
        addressBookService.addAddress(addressBook);
        return Result.success();
    }

    /**
     * 查询所有地址
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询所有地址")
    public Result<List<AddressBook>> QueryAllAddresses() {
        log.info("## 查询所有地址");
        return Result.success(addressBookService.QueryAllAddresses());
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("## 设置默认地址: {}", addressBook);
        addressBookService.setDefaultAddress(addressBook);
        return Result.success();
    }

    /**
     * 查询默认地址
     *
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefaultAddress() {
        log.info("## 查询默认地址");
        return Result.success(addressBookService.getDefaultAddress());
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getAddressById(Long id) {
        return Result.success(addressBookService.getAddressById(id));
    }


    /**
     * 根据id修改地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result editAddressById(@RequestBody AddressBook addressBook) {
        log.info("## 修改地址: {}", addressBook);
        addressBookService.editAddressById(addressBook);
        return Result.success(
        );
    }

    /**
     * 根据id删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteAddressById(Long id) {
        addressBookService.deleteAddressById(id);
        return Result.success();
    }
}
