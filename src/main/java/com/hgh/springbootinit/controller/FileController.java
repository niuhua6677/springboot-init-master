package com.hgh.springbootinit.controller;

import cn.hutool.core.util.StrUtil;
import com.hgh.springbootinit.common.BaseResponse;
import com.hgh.springbootinit.common.ErrorCode;
import com.hgh.springbootinit.common.ResultUtils;
import com.hgh.springbootinit.constant.FileConstant;
import com.hgh.springbootinit.exception.BusinessException;
import com.hgh.springbootinit.manager.CosManager;
import com.hgh.springbootinit.model.dto.file.UploadFileRequest;
import com.hgh.springbootinit.model.entity.User;
import com.hgh.springbootinit.model.enums.FileUploadBizEnum;
import com.hgh.springbootinit.service.UserService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口控制器
 *
 * @author hgh
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService; // 用户服务接口，用于获取当前登录用户的信息

    @Resource
    private CosManager cosManager; // 对象存储管理器，用于上传文件到云端存储

    /**
     * 文件上传接口
     *
     * @param multipartFile 上传的文件
     * @param uploadFileRequest 请求参数对象，包含业务类型等信息
     * @param request HTTP请求对象
     * @return 包含文件上传结果的BaseResponse对象
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest,
                                           HttpServletRequest request) {
        // 获取业务类型字符串
        String biz = uploadFileRequest.getBiz();

        // 将业务类型字符串转换成枚举类型
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);

        // 校验业务类型是否合法
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 校验文件是否合法
        validFile(multipartFile, fileUploadBizEnum);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 生成随机UUID，用于文件命名
        String uuid = RandomStringUtils.randomAlphanumeric(8);

        // 构造新的文件名
        String filename = uuid + "-" + multipartFile.getOriginalFilename();

        // 构造文件路径
        String filepath = String.format("/%s/%s/%s",
                fileUploadBizEnum.getValue(),
                loginUser.getId(),
                filename);

        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(filepath, null);

            // 将上传的文件保存到临时文件
            multipartFile.transferTo(file);

            // 上传文件到对象存储
            cosManager.putObject(filepath, file);

            // 返回可访问的文件URL
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (IOException e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件的合法性
     *
     * @param multipartFile 上传的文件
     * @param fileUploadBizEnum 文件上传的业务类型枚举
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 获取文件大小
        long fileSize = multipartFile.getSize();

        // 获取文件后缀
        String fileSuffix = StrUtil.subAfter(multipartFile.getOriginalFilename(), ".", true);

        // 文件大小限制
        final long ONE_M = 1024 * 1024L;

        // 根据不同的业务类型校验文件
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}