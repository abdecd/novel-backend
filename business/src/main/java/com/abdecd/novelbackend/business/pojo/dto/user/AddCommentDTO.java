package com.abdecd.novelbackend.business.pojo.dto.user;

import cn.hutool.dfa.SensitiveUtil;
import com.abdecd.novelbackend.business.pojo.entity.UserComment;
import com.abdecd.novelbackend.common.constant.DTOConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.tokenlogin.common.context.UserContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Data
public class AddCommentDTO {
    @NotNull
    private Integer novelId;
    @NotNull
    @Schema(description = "目标评论id，对小说写-1")
    private Long toId;
    @NotBlank
    @Length(min = 1, max = DTOConstant.COMMENT_LENGTH_MAX)
    private String content;

    static {
        // 初始化敏感词
        var classPathResource = new ClassPathResource("sensitive/comment.txt");
        try (var reader = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()))) {
            var sensitiveWords = reader.lines().toList();
            SensitiveUtil.init(sensitiveWords);
        } catch (IOException e) {
            System.out.println("敏感词文件不存在");
        }
    }

    public UserComment toEntity() {
        UserComment userComment = new UserComment();
        BeanUtils.copyProperties(this, userComment);
        userComment.setUserId(UserContext.getUserId());
        userComment.setTimestamp(LocalDateTime.now());
        userComment.setStatus(StatusConstant.ENABLE);
        userComment.setContent(SensitiveUtil.sensitiveFilter(content));
        return userComment;
    }
}
