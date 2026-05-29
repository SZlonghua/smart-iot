package net.lab1024.sa.admin.module.business.product.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.lab1024.sa.admin.module.business.product.dao.ProductDao;
import net.lab1024.sa.admin.module.business.product.domain.entity.ProductEntity;
import org.springframework.stereotype.Service;

/**
 * 产品 Manager
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Service
public class ProductManager extends ServiceImpl<ProductDao, ProductEntity> {
}
