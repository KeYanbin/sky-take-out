package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

public interface EmployeeService extends IService<Employee> {

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     *
     * @param employeeDTO 员工dto
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO 员工分页查询条件
     * @return Result < page Result >
     */
    PageResult employeePageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    /**
     * 更新员工状态
     *
     * @param status 状态
     * @param id     id
     * @return 后端统一返回结果
     */
    void updateStatus(Integer status, Long id);

    /**
     * 按id查询
     *
     * @param id id
     * @return 后端统一返回结果
     */
    Employee selectById(Long id);

    /**
     * 更新员工
     *
     * @param employee 员工dto
     * @return 后端统一返回结果
     */
    void updateEmployee(Employee employee);
}
