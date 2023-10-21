package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
//        Employee employee = employeeMapper.getByUsername(username);
        Employee employee = lambdaQuery().eq(Employee::getUsername, username).one();

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传来的密码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO 员工dto
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);

        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置创建人id和修改人id
        Long emID = BaseContext.getCurrentId();
        employee.setCreateUser(emID);
        employee.setUpdateUser(emID);

        save(employee);
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO 员工分页查询条件
     * @return Result < page Result >
     */
    @Override
    public PageResult employeePageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 分页参数
        Page<Employee> page = Page.of(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //name 要判断不为null
        Page<Employee> p = lambdaQuery()
                .like(StrUtil.isNotBlank(employeePageQueryDTO.getName()), Employee::getName, employeePageQueryDTO.getName())
                .page(page);

        return new PageResult(p.getTotal(), p.getRecords());

    }


    /**
     * 更新员工状态
     *
     * @param status 状态
     * @param id     id
     * @return 后端统一返回结果
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        // status 如果是0 则禁用
        status = status == StatusConstant.DISABLE ? StatusConstant.DISABLE : StatusConstant.ENABLE;


        lambdaUpdate().eq(Employee::getId, id).set(Employee::getStatus, status).update();
    }

    /**
     * 按id查询
     *
     * @param id id
     * @return 后端统一返回结果
     */
    @Override
    public Employee selectById(Long id) {
        return getById(id);
    }

    /**
     * 更新员工
     *
     * @param employee 员工dto
     * @return 后端统一返回结果
     */
    @Override
    public void updateEmployee(Employee employee) {
        updateById(employee);
    }

}
