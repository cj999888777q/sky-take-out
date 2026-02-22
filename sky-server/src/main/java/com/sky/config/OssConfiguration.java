package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 配置类，用于创建AliOssUtil对象
 */
@Configuration
public class OssConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OssConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties)
    {
          log.info("创建阿里云上传文件工具类对象:{}",aliOssProperties);
          return new AliOssUtil(aliOssProperties.getEndpoint(),aliOssProperties.getAccessKeyId()
          ,aliOssProperties.getAccessKeySecret(),aliOssProperties.getBucketName());
    }
}
